package com.zenmanage.kotlin

import com.zenmanage.kotlin.api.ApiClient
import com.zenmanage.kotlin.cache.Cache
import com.zenmanage.kotlin.cache.FileSystemCache
import com.zenmanage.kotlin.cache.InMemoryCache
import com.zenmanage.kotlin.cache.NullCache
import com.zenmanage.kotlin.config.CacheBackend
import com.zenmanage.kotlin.config.Config
import com.zenmanage.kotlin.errors.ConfigurationException
import com.zenmanage.kotlin.flags.FlagManager
import com.zenmanage.kotlin.rules.RuleEngine

public class Zenmanage(config: Config) {
    private val flagManager: FlagManager

    init {
        val cache = createCache(config)

        val apiClient = ApiClient(
            environmentToken = config.environmentToken,
            apiEndpoint = config.apiEndpoint,
            logger = config.logger,
            usageReportingEnabled = config.usageReportingEnabled,
            sdkVersion = config.sdkVersion,
            clientAgent = config.clientAgent
        )

        val ruleEngine = RuleEngine()
        flagManager = FlagManager(apiClient, cache, ruleEngine, config.cacheTtlSeconds, config.logger)
    }

    public fun flags(): FlagManager = flagManager

    private fun createCache(config: Config): Cache {
        config.customCache?.let { return it }

        return when (config.cacheBackend) {
            CacheBackend.MEMORY -> InMemoryCache()
            CacheBackend.NULL -> NullCache()
            CacheBackend.FILESYSTEM -> {
                val directory = config.cacheDirectory?.takeIf { it.isNotBlank() }
                    ?: throw ConfigurationException("Cache directory is required for filesystem cache")
                FileSystemCache(directory)
            }

            CacheBackend.SHARED_PREFERENCES -> {
                throw ConfigurationException(
                    "SharedPreferences cache requires a custom cache instance. Use withCache(SharedPreferencesCache(...))"
                )
            }
        }
    }
}
