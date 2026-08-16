package com.zenmanage.kotlin.cache

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files

class FileSystemCacheTest {
    @Test
    fun persistsAndLoadsValues() {
        val directory = Files.createTempDirectory("zenmanage-fs-cache")
        val cache = FileSystemCache(directory)

        cache.set("rules", "{\"v\":1}", 60)
        assertThat(cache.get("rules")).isEqualTo("{\"v\":1}")
    }

    @Test
    fun clearRemovesPersistedFiles() {
        val directory = Files.createTempDirectory("zenmanage-fs-cache-clear")
        val cache = FileSystemCache(directory)

        cache.set("a", "x", 60)
        cache.set("b", "y", 60)
        cache.clear()

        assertThat(cache.has("a")).isFalse()
        assertThat(cache.has("b")).isFalse()
    }
}
