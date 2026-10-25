package com.zenmanage.kotlin.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zenmanage.kotlin.config.Logger
import com.zenmanage.kotlin.context.Context
import com.zenmanage.kotlin.errors.FetchRulesException
import com.zenmanage.kotlin.errors.InvalidRulesException
import com.zenmanage.kotlin.flags.RulesResponse
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

public open class ApiClient(
    environmentToken: String,
    private val apiEndpoint: String,
    private val logger: Logger,
    private val usageReportingEnabled: Boolean,
    sdkVersion: String,
    clientAgent: String,
    private val httpClient: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
    private val objectMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
) {
    private val baseHeaders: Map<String, String> = mapOf(
        "Accept" to "application/json",
        "Content-Type" to "application/json",
        "X-ZEN-API-KEY" to environmentToken,
        "X-ZEN-CLIENT-AGENT" to "$clientAgent/$sdkVersion"
    )

    public open fun getRules(): RulesResponse {
        var lastException: Exception? = null

        repeat(MAX_RETRIES) { index ->
            try {
                if (index > 0) {
                    val delay = BASE_RETRY_DELAY_MS * (1 shl (index - 1))
                    Thread.sleep(delay.toLong())
                }

                val cdnUrl = getCdnRulesUrl()
                val request = HttpRequest.newBuilder()
                    .uri(URI.create(cdnUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .header("Accept", "application/json")
                    .header("X-ZEN-CLIENT-AGENT", baseHeaders.getValue("X-ZEN-CLIENT-AGENT"))
                    .build()

                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                if (response.statusCode() !in 200..299) {
                    throw FetchRulesException("CDN request failed with status ${response.statusCode()}", response.statusCode())
                }

                val rules = objectMapper.readValue(response.body(), RulesResponse::class.java)
                if (rules.version == null || rules.flags == null) {
                    throw InvalidRulesException("Invalid rules response format from CDN")
                }

                return rules
            } catch (exception: InterruptedException) {
                Thread.currentThread().interrupt()
                throw FetchRulesException("Interrupted while fetching rules", exception)
            } catch (exception: Exception) {
                lastException = exception
                logger.warn("Failed to fetch rules attempt ${index + 1}/$MAX_RETRIES")
            }
        }

        throw FetchRulesException("Failed to fetch rules after retries", lastException ?: RuntimeException("Unknown error"))
    }

    public open fun reportUsage(key: String, context: Context?, defaultValue: Any? = null): Unit {
        if (!usageReportingEnabled) {
            return
        }

        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create("$apiEndpoint/v1/flags/$key/usage"))
            .timeout(Duration.ofSeconds(5))
            .POST(HttpRequest.BodyPublishers.noBody())

        baseHeaders.forEach { (header, value) -> requestBuilder.header(header, value) }

        if (context != null && shouldSendContext(context)) {
            runCatching {
                requestBuilder.header("X-ZEN-CONTEXT", objectMapper.writeValueAsString(context.toMap()))
            }.onFailure {
                logger.debug("Failed to encode usage context")
            }
        }

        if (defaultValue != null) {
            runCatching {
                requestBuilder.header("X-ZEN-DEFAULT-VALUE", objectMapper.writeValueAsString(mapOf(key to defaultValue)))
            }.onFailure {
                logger.debug("Failed to encode usage default value")
            }
        }

        httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.discarding())
            .thenApply { null }
            .exceptionally {
                logger.debug("Failed to report usage: ${it.message}")
                null
            }
    }

    private fun getCdnRulesUrl(): String {
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create("$apiEndpoint$RULES_PATH"))
            .timeout(Duration.ofSeconds(10))
            .GET()

        baseHeaders.forEach { (header, value) -> requestBuilder.header(header, value) }

        val response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        if (response.statusCode() !in 200..299) {
            throw FetchRulesException("API metadata request failed with status ${response.statusCode()}", response.statusCode())
        }

        val payload = objectMapper.readValue(response.body(), object : TypeReference<Map<String, Any?>>() {})
        val data = payload["data"] as? Map<*, *> ?: throw InvalidRulesException("API response missing data field")
        val cdn = data["cdn"]?.toString() ?: throw InvalidRulesException("API response missing cdn field")
        val path = data["path"]?.toString() ?: throw InvalidRulesException("API response missing path field")
        return "$cdn$path"
    }

    private fun shouldSendContext(context: Context): Boolean {
        return !(context.type == "anonymous" && context.name == null && context.identifier == null && context.getAttributes().isEmpty())
    }

    private companion object {
        private const val RULES_PATH: String = "/v1/flag-json"
        private const val MAX_RETRIES: Int = 3
        private const val BASE_RETRY_DELAY_MS: Int = 100
    }
}
