package com.zenmanage.kotlin.cache

import android.content.SharedPreferences
import kotlin.math.max

public class SharedPreferencesCache(
    private val sharedPreferences: SharedPreferences,
    private val namespace: String = "zenmanage"
) : Cache {
    override fun get(key: String): String? {
        val prefKey = valueKey(key)
        val expiry = sharedPreferences.getLong(expiryKey(key), 0L)
        if (expiry <= 0L || expiry < nowEpochMillis()) {
            delete(key)
            return null
        }
        return sharedPreferences.getString(prefKey, null)
    }

    override fun set(key: String, value: String, ttlSeconds: Long): Unit {
        val safeTtl = max(ttlSeconds, 1L)
        val expiry = nowEpochMillis() + (safeTtl * 1000L)
        sharedPreferences.edit()
            .putString(valueKey(key), value)
            .putLong(expiryKey(key), expiry)
            .apply()
    }

    override fun has(key: String): Boolean = get(key) != null

    override fun delete(key: String): Unit {
        sharedPreferences.edit()
            .remove(valueKey(key))
            .remove(expiryKey(key))
            .apply()
    }

    override fun clear(): Unit {
        val keys = sharedPreferences.all.keys.filter { it.startsWith("$namespace:") }
        val editor = sharedPreferences.edit()
        keys.forEach(editor::remove)
        editor.apply()
    }

    private fun valueKey(key: String): String = "$namespace:$key:value"

    private fun expiryKey(key: String): String = "$namespace:$key:expires"

    private fun nowEpochMillis(): Long = System.currentTimeMillis()
}
