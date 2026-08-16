package com.zenmanage.kotlin.api

import com.sun.net.httpserver.HttpServer
import com.zenmanage.kotlin.config.NullLogger
import com.zenmanage.kotlin.context.Context
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

class ApiClientTest {
    private lateinit var server: HttpServer
    private lateinit var capturedHeaders: BlockingQueue<Map<String, List<String>>>
    private lateinit var endpoint: String

    @BeforeEach
    fun startServer() {
        capturedHeaders = ArrayBlockingQueue(10)
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            capturedHeaders.offer(exchange.requestHeaders)
            exchange.sendResponseHeaders(200, -1)
            exchange.close()
        }
        server.start()
        endpoint = "http://127.0.0.1:${server.address.port}"
    }

    @AfterEach
    fun stopServer() {
        server.stop(0)
    }

    @Test
    fun reportUsageSendsDefaultValueHeader() {
        val client = ApiClient("srv_test", endpoint, NullLogger(), true, "0.1.0", "zenmanage-kotlin")

        client.reportUsage("new-ui", null, true)

        val headers = capturedHeaders.poll(5, TimeUnit.SECONDS)
        assertThat(headers).isNotNull()
        assertThat(headers!!["X-Default-Value"]?.get(0)).isEqualTo("{\"new-ui\":true}")
    }

    @Test
    fun reportUsageSendsNonBooleanDefaultValueHeader() {
        val client = ApiClient("srv_test", endpoint, NullLogger(), true, "0.1.0", "zenmanage-kotlin")

        client.reportUsage("num-flag", null, 42)

        val headers = capturedHeaders.poll(5, TimeUnit.SECONDS)
        assertThat(headers).isNotNull()
        assertThat(headers!!["X-Default-Value"]?.get(0)).isEqualTo("{\"num-flag\":42}")
    }

    @Test
    fun reportUsageOmitsDefaultValueHeaderWhenNotProvided() {
        val client = ApiClient("srv_test", endpoint, NullLogger(), true, "0.1.0", "zenmanage-kotlin")

        client.reportUsage("new-ui", null)

        val headers = capturedHeaders.poll(5, TimeUnit.SECONDS)
        assertThat(headers).isNotNull()
        assertThat(headers!!.containsKey("X-Default-Value")).isFalse()
    }

    @Test
    fun reportUsageSendsBothContextAndDefaultValueHeaders() {
        val client = ApiClient("srv_test", endpoint, NullLogger(), true, "0.1.0", "zenmanage-kotlin")

        client.reportUsage("num-flag", Context.single("user", "u-1"), 42)

        val headers = capturedHeaders.poll(5, TimeUnit.SECONDS)
        assertThat(headers).isNotNull()
        assertThat(headers!!["X-Default-Value"]?.get(0)).isEqualTo("{\"num-flag\":42}")
        assertThat(headers.containsKey("X-Zenmanage-Context")).isTrue()
    }

    @Test
    fun reportUsageDisabledSkipsRequest() {
        val client = ApiClient("srv_test", endpoint, NullLogger(), false, "0.1.0", "zenmanage-kotlin")

        client.reportUsage("new-ui", null, true)

        assertThat(capturedHeaders.poll(500, TimeUnit.MILLISECONDS)).isNull()
    }
}
