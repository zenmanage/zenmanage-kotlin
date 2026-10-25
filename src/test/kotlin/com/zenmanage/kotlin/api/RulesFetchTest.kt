package com.zenmanage.kotlin.api

import com.sun.net.httpserver.HttpServer
import com.zenmanage.kotlin.ZenmanageSDK
import com.zenmanage.kotlin.config.NullLogger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

/**
 * Exercises the two-step rules fetch (metadata, then CDN payload) against a local server and checks
 * that every request identifies the SDK with its real release version.
 */
class RulesFetchTest {
    private lateinit var server: HttpServer
    private lateinit var endpoint: String
    private val agents = ConcurrentHashMap.newKeySet<String>()

    @BeforeEach
    fun startServer() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/v1/flag-json") { exchange ->
            agents.add(exchange.requestHeaders.getFirst("X-ZEN-CLIENT-AGENT") ?: "")
            respond(exchange, """{"data":{"cdn":"$endpoint","path":"/rules.json"}}""")
        }
        server.createContext("/rules.json") { exchange ->
            agents.add(exchange.requestHeaders.getFirst("X-ZEN-CLIENT-AGENT") ?: "")
            respond(exchange, """{"version":"1","flags":[]}""")
        }
        server.start()
        endpoint = "http://127.0.0.1:${server.address.port}"
    }

    @AfterEach
    fun stopServer() {
        server.stop(0)
    }

    private fun respond(exchange: com.sun.net.httpserver.HttpExchange, body: String) {
        val bytes = body.toByteArray()
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(200, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    @Test
    fun fetchesRulesFromTheCdnPayloadTheMetadataPointsAt() {
        val client = ApiClient("srv_test", endpoint, NullLogger(), false, ZenmanageSDK.VERSION, "zenmanage-kotlin")

        val response = client.getRules()

        assertThat(response.version).isEqualTo("1")
        assertThat(response.flags).isEmpty()
    }

    @Test
    fun everyRequestReportsTheReleaseVersionInTheClientAgent() {
        val client = ApiClient("srv_test", endpoint, NullLogger(), false, ZenmanageSDK.VERSION, "zenmanage-kotlin")

        client.getRules()

        assertThat(agents).containsOnly("zenmanage-kotlin/${ZenmanageSDK.VERSION}")
    }
}
