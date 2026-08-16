package com.zenmanage.kotlin.cache

public interface Cache {
    public fun get(key: String): String?

    public fun set(key: String, value: String, ttlSeconds: Long)

    public fun has(key: String): Boolean

    public fun delete(key: String)

    public fun clear()
}
