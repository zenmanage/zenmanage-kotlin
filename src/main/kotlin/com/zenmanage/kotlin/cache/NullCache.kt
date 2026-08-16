package com.zenmanage.kotlin.cache

public class NullCache : Cache {
    override fun get(key: String): String? = null

    override fun set(key: String, value: String, ttlSeconds: Long): Unit = Unit

    override fun has(key: String): Boolean = false

    override fun delete(key: String): Unit = Unit

    override fun clear(): Unit = Unit
}
