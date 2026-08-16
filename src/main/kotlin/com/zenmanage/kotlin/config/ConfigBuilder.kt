package com.zenmanage.kotlin.config

import com.zenmanage.kotlin.cache.Cache
import com.zenmanage.kotlin.errors.ConfigurationException

public class ConfigBuilder private constructor() {
    private var environmentToken: String? = null
    private var cacheTtlSeconds: Int = 3600
    private var cacheBackend: CacheBackend = CacheBackend.MEMORY
    private var cacheDirectory: String? = null
    private var usageReportingEnabled: Boolean = true
    private var apiEndpoint: String = DEFAULT_API_ENDPOINT
    private var logger: Logger? = null
    private var customCache: Cache? = null
    private var sdkVersion: String = "0.1.0"
    private var clientAgent: String = "zenmanage-kotlin"

    public fun withEnvironmentToken(token: String): ConfigBuilder {
        environmentToken = token
        return this
    }

    public fun withCacheTtlSeconds(ttlSeconds: Int): ConfigBuilder {
        cacheTtlSeconds = ttlSeconds
        return this
    }

    public fun withCacheBackend(backend: String): ConfigBuilder {
        cacheBackend = when (backend.lowercase()) {
            "memory" -> CacheBackend.MEMORY
            "filesystem" -> CacheBackend.FILESYSTEM
            "null" -> CacheBackend.NULL
            "shared_preferences", "sharedpreferences" -> CacheBackend.SHARED_PREFERENCES
            else -> throw ConfigurationException("Invalid cache backend: $backend")
        }
        return this
    }

    public fun withCacheDirectory(directory: String): ConfigBuilder {
        cacheDirectory = directory
        return this
    }

    public fun withUsageReporting(enabled: Boolean): ConfigBuilder {
        usageReportingEnabled = enabled
        return this
    }

    public fun withApiEndpoint(endpoint: String): ConfigBuilder {
        apiEndpoint = endpoint
        return this
    }

    public fun withLogger(customLogger: Logger): ConfigBuilder {
        logger = customLogger
        return this
    }

    public fun withCache(cache: Cache): ConfigBuilder {
        customCache = cache
        return this
    }

    public fun withSdkVersion(version: String): ConfigBuilder {
        sdkVersion = version
        return this
    }

    public fun withClientAgent(agent: String): ConfigBuilder {
        clientAgent = agent
        return this
    }

    public fun build(): Config {
        val token = environmentToken?.takeIf { it.isNotBlank() }
            ?: throw ConfigurationException("Environment token is required")

        validateEnvironmentToken(token)

        if (cacheTtlSeconds <= 0) {
            throw ConfigurationException("Cache TTL must be greater than zero")
        }

        if (cacheBackend == CacheBackend.FILESYSTEM && customCache == null && cacheDirectory.isNullOrBlank()) {
            throw ConfigurationException("Cache directory is required for filesystem cache")
        }

        return Config(
            environmentToken = token,
            cacheTtlSeconds = cacheTtlSeconds,
            cacheBackend = cacheBackend,
            cacheDirectory = cacheDirectory,
            usageReportingEnabled = usageReportingEnabled,
            apiEndpoint = apiEndpoint,
            logger = logger ?: NullLogger(),
            customCache = customCache,
            sdkVersion = sdkVersion,
            clientAgent = clientAgent
        )
    }

    private fun validateEnvironmentToken(token: String): Unit {
        when {
            token.startsWith(CLIENT_KEY_PREFIX) -> {
                throw ConfigurationException(
                    "Invalid environment token for Kotlin/Android runtime: client key provided. Use server or mobile key"
                )
            }

            token.startsWith(MOBILE_KEY_PREFIX) || token.startsWith(SERVER_KEY_PREFIX) -> Unit

            else -> {
                throw ConfigurationException("Invalid environment token format. Expected srv_ or mob_ prefix")
            }
        }
    }

    public companion object {
        private const val DEFAULT_API_ENDPOINT: String = "https://api.zenmanage.com"
        private const val SERVER_KEY_PREFIX: String = "srv_"
        private const val CLIENT_KEY_PREFIX: String = "cli_"
        private const val MOBILE_KEY_PREFIX: String = "mob_"

        public fun create(): ConfigBuilder = ConfigBuilder()

        public fun fromEnvironment(environment: Map<String, String> = System.getenv()): ConfigBuilder {
            val builder = create()

            environment["ZENMANAGE_ENVIRONMENT_TOKEN"]?.let(builder::withEnvironmentToken)
            environment["ZENMANAGE_CACHE_TTL"]?.toIntOrNull()?.let(builder::withCacheTtlSeconds)
            environment["ZENMANAGE_CACHE_BACKEND"]?.let(builder::withCacheBackend)
            environment["ZENMANAGE_CACHE_DIR"]?.let(builder::withCacheDirectory)
            environment["ZENMANAGE_ENABLE_USAGE_REPORTING"]?.let {
                builder.withUsageReporting(it.equals("true", ignoreCase = true) || it == "1")
            }
            environment["ZENMANAGE_API_ENDPOINT"]?.let(builder::withApiEndpoint)

            return builder
        }
    }
}
