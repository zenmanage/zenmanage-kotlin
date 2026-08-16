package com.zenmanage.kotlin.flags

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FlagTest {
    @Test
    fun booleanFlagHelpersWork() {
        val flag = Flag(
            version = "1",
            type = FlagType.BOOLEAN,
            key = "feature",
            name = "feature",
            target = FlagTarget(value = TargetValue(value = RawFlagValue(booleanValue = true))),
            rules = emptyList(),
            rollout = null
        )

        assertThat(flag.isEnabled()).isTrue()
        assertThat(flag.asBool()).isTrue()
        assertThat(flag.asNumber()).isEqualTo(1.0)
        assertThat(flag.asString()).isEqualTo("true")
    }

    @Test
    fun numericAndStringConversionFallbacksWork() {
        val numberFlag = Flag(
            version = "1",
            type = FlagType.NUMBER,
            key = "timeout",
            name = "timeout",
            target = FlagTarget(value = TargetValue(value = RawFlagValue(numberValue = 42.0))),
            rules = emptyList(),
            rollout = null
        )

        assertThat(numberFlag.asNumber()).isEqualTo(42.0)

        val stringFlag = numberFlag.copy(
            type = FlagType.STRING,
            target = FlagTarget(value = TargetValue(value = RawFlagValue(stringValue = "abc")))
        )

        assertThat(stringFlag.asBool()).isFalse()
        assertThat(stringFlag.asString()).isEqualTo("abc")
    }
}
