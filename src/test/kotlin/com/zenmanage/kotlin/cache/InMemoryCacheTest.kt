package com.zenmanage.kotlin.cache

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InMemoryCacheTest {
    @Test
    fun setGetAndDeleteWork() {
        val cache = InMemoryCache()
        cache.set("k", "v", 60)

        assertThat(cache.get("k")).isEqualTo("v")
        assertThat(cache.has("k")).isTrue()

        cache.delete("k")
        assertThat(cache.get("k")).isNull()
    }

    @Test
    fun clearRemovesAllEntries() {
        val cache = InMemoryCache()
        cache.set("a", "1", 60)
        cache.set("b", "2", 60)

        cache.clear()

        assertThat(cache.has("a")).isFalse()
        assertThat(cache.has("b")).isFalse()
    }
}
