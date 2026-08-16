package com.zenmanage.kotlin.rollout

import java.nio.charset.StandardCharsets
import java.util.zip.CRC32

public object RolloutBucketer {
    public fun crc32b(input: String): Long {
        val crc = CRC32()
        crc.update(input.toByteArray(StandardCharsets.UTF_8))
        return crc.value
    }

    public fun isInBucket(salt: String, contextIdentifier: String?, percentage: Double): Boolean {
        require(percentage in 0.0..100.0) { "Percentage must be between 0 and 100" }
        if (contextIdentifier.isNullOrBlank()) {
            return false
        }

        val hash = crc32b("$salt:$contextIdentifier")
        val bucket = hash % 100
        return bucket < percentage
    }
}
