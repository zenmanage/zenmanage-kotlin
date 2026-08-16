package com.zenmanage.kotlin.config

import com.zenmanage.kotlin.errors.ConfigurationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ConfigBuilderTest {
    @Test
    fun buildRequiresToken() {
        assertThrows(ConfigurationException::class.java) {
            ConfigBuilder.create().build()
        }
    }

    @Test
    fun buildRejectsClientToken() {
        assertThrows(ConfigurationException::class.java) {
            ConfigBuilder.create().withEnvironmentToken("cli_abc").build()
        }
    }

    @Test
    fun buildAcceptsMobileToken() {
        val config = ConfigBuilder.create()
            .withEnvironmentToken("mob_abc")
            .build()

        assertEquals("mob_abc", config.environmentToken)
    }

    @Test
    fun fromEnvironmentMapsValues() {
        val config = ConfigBuilder.fromEnvironment(
            mapOf(
                "ZENMANAGE_ENVIRONMENT_TOKEN" to "srv_env",
                "ZENMANAGE_CACHE_TTL" to "123",
                "ZENMANAGE_CACHE_BACKEND" to "null",
                "ZENMANAGE_ENABLE_USAGE_REPORTING" to "false",
                "ZENMANAGE_API_ENDPOINT" to "https://example.com"
            )
        ).build()

        assertEquals("srv_env", config.environmentToken)
        assertEquals(123, config.cacheTtlSeconds)
        assertEquals(CacheBackend.NULL, config.cacheBackend)
        assertEquals("https://example.com", config.apiEndpoint)
    }
}
