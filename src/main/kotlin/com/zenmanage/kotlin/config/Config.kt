package com.zenmanage.kotlin.config

import com.zenmanage.kotlin.cache.Cache

public data class Config(
    val environmentToken: String,
    val cacheTtlSeconds: Int,
    val cacheBackend: CacheBackend,
    val cacheDirectory: String?,
    val usageReportingEnabled: Boolean,
    val apiEndpoint: String,
    val logger: Logger,
    val customCache: Cache?,
    val sdkVersion: String,
    val clientAgent: String
)
