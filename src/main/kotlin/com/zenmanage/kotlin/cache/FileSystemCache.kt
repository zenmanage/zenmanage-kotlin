package com.zenmanage.kotlin.cache

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.Base64

public class FileSystemCache(private val cacheDirectory: Path) : Cache {
    public constructor(cacheDirectory: String) : this(Path.of(cacheDirectory))

    init {
        runCatching { Files.createDirectories(cacheDirectory) }
    }

    override fun get(key: String): String? {
        val filePath = cacheFilePath(key)
        if (!Files.exists(filePath)) {
            return null
        }

        return runCatching {
            val content = Files.readString(filePath, StandardCharsets.UTF_8)
            val separator = content.indexOf('\n')
            if (separator < 0) {
                delete(key)
                return null
            }

            val expiresAt = content.substring(0, separator).toLongOrNull() ?: return null
            val value = content.substring(separator + 1)
            if (expiresAt < Instant.now().toEpochMilli()) {
                delete(key)
                return null
            }

            value
        }.getOrNull()
    }

    override fun set(key: String, value: String, ttlSeconds: Long): Unit {
        val filePath = cacheFilePath(key)
        val expiresAt = Instant.now().toEpochMilli() + (ttlSeconds * 1000L)
        runCatching {
            Files.writeString(filePath, "$expiresAt\n$value", StandardCharsets.UTF_8)
        }
    }

    override fun has(key: String): Boolean = get(key) != null

    override fun delete(key: String): Unit {
        runCatching { Files.deleteIfExists(cacheFilePath(key)) }
    }

    override fun clear(): Unit {
        runCatching {
            Files.list(cacheDirectory).use { paths ->
                paths
                    .filter { it.fileName.toString().endsWith(".json") }
                    .forEach { Files.deleteIfExists(it) }
            }
        }
    }

    private fun cacheFilePath(key: String): Path {
        val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(key.toByteArray(StandardCharsets.UTF_8))
        return cacheDirectory.resolve("$encoded.json")
    }
}
