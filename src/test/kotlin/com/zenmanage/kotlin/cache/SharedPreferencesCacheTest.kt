package com.zenmanage.kotlin.cache

import android.content.SharedPreferences
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SharedPreferencesCacheTest {
    @Test
    fun setGetDeleteAndClearWork() {
        val prefs = FakeSharedPreferences()
        val cache = SharedPreferencesCache(prefs)

        cache.set("rules", "payload", 60)
        assertThat(cache.get("rules")).isEqualTo("payload")

        cache.delete("rules")
        assertThat(cache.get("rules")).isNull()

        cache.set("a", "x", 60)
        cache.set("b", "y", 60)
        cache.clear()

        assertThat(cache.has("a")).isFalse()
        assertThat(cache.has("b")).isFalse()
    }

    private class FakeSharedPreferences : SharedPreferences {
        private val data = linkedMapOf<String, Any>()

        override fun getAll(): MutableMap<String, *> = data.toMutableMap()

        override fun getString(key: String?, defValue: String?): String? =
            (key?.let { data[it] } as? String) ?: defValue

        @Suppress("UNCHECKED_CAST")
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            (key?.let { data[it] } as? MutableSet<String>) ?: defValues

        override fun getInt(key: String?, defValue: Int): Int = (key?.let { data[it] } as? Int) ?: defValue

        override fun getLong(key: String?, defValue: Long): Long = (key?.let { data[it] } as? Long) ?: defValue

        override fun getFloat(key: String?, defValue: Float): Float = (key?.let { data[it] } as? Float) ?: defValue

        override fun getBoolean(key: String?, defValue: Boolean): Boolean =
            (key?.let { data[it] } as? Boolean) ?: defValue

        override fun contains(key: String?): Boolean = key != null && data.containsKey(key)

        override fun edit(): SharedPreferences.Editor = Editor(data)

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
            // Not needed for tests.
        }

        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
            // Not needed for tests.
        }

        private class Editor(private val data: MutableMap<String, Any>) : SharedPreferences.Editor {
            private val updates = linkedMapOf<String, Any?>()
            private var clear = false

            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) updates[key] = value
                return this
            }

            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
                if (key != null) updates[key] = values
                return this
            }

            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                if (key != null) updates[key] = value
                return this
            }

            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                if (key != null) updates[key] = value
                return this
            }

            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                if (key != null) updates[key] = value
                return this
            }

            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                if (key != null) updates[key] = value
                return this
            }

            override fun remove(key: String?): SharedPreferences.Editor {
                if (key != null) updates[key] = null
                return this
            }

            override fun clear(): SharedPreferences.Editor {
                clear = true
                return this
            }

            override fun commit(): Boolean {
                apply()
                return true
            }

            override fun apply() {
                if (clear) {
                    data.clear()
                }

                updates.forEach { (key, value) ->
                    if (value == null) {
                        data.remove(key)
                    } else {
                        data[key] = value
                    }
                }
            }
        }
    }
}
