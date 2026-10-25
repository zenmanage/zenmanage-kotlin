package com.zenmanage.kotlin.rollout

import com.zenmanage.kotlin.errors.EvaluationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class RolloutBucketerTest {
    @Test
    fun crc32IsDeterministic() {
        val a = RolloutBucketer.crc32b("abc")
        val b = RolloutBucketer.crc32b("abc")
        assertEquals(a, b)
    }

    @Test
    fun nullIdentifierIsNeverInBucket() {
        assertFalse(RolloutBucketer.isInBucket("salt", null, 10.0))
    }

    @Test
    fun invalidPercentageThrows() {
        assertThrows(EvaluationException::class.java) {
            RolloutBucketer.isInBucket("s", "u", 101.0)
        }
    }

    @Test
    fun negativePercentageThrows() {
        assertThrows(EvaluationException::class.java) {
            RolloutBucketer.isInBucket("s", "u", -0.5)
        }
    }
}
