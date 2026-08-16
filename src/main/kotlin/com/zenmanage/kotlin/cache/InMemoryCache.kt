package com.zenmanage.kotlin.cache

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

public class InMemoryCache : Cache {
    private data class CacheItem(val value: String, val expiresAtEpochMillis: Long)

    private val storage: MutableMap<String, CacheItem> = ConcurrentHashMap()

    override fun get(key: String): String? {
        val item = storage[key] ?: return null
        if (item.expiresAtEpochMillis < Instant.now().toEpochMilli()) {
            storage.remove(key)
            return null
        }
        return item.value
    }

    override fun set(key: String, value: String, ttlSeconds: Long): Unit {
        val expiresAt = Instant.now().toEpochMilli() + (ttlSeconds * 1000L)
        storage[key] = CacheItem(value, expiresAt)
    }

    override fun has(key: String): Boolean = get(key) != null

    override fun delete(key: String): Unit {
        storage.remove(key)
    }

    override fun clear(): Unit {
        storage.clear()
    }
}
