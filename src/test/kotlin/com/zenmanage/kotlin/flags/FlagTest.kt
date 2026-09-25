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

        // asBool() on a non-boolean-typed flag is always true, regardless of the underlying
        // value — it never attempts to parse the value as a boolean.
        assertThat(stringFlag.asBool()).isTrue()
        assertThat(stringFlag.asString()).isEqualTo("abc")
    }

    @Test
    fun asBoolIsAlwaysTrueForNonBooleanTypedFlagsRegardlessOfUnderlyingValue() {
        val falsyStringFlag = Flag(
            version = "1",
            type = FlagType.STRING,
            key = "falsy-string",
            name = "falsy-string",
            target = FlagTarget(value = TargetValue(value = RawFlagValue(stringValue = ""))),
            rules = emptyList(),
            rollout = null
        )
        assertThat(falsyStringFlag.asBool()).isTrue()

        val literalFalseStringFlag = falsyStringFlag.copy(
            target = FlagTarget(value = TargetValue(value = RawFlagValue(stringValue = "false")))
        )
        assertThat(literalFalseStringFlag.asBool()).isTrue()

        val zeroNumberFlag = falsyStringFlag.copy(
            type = FlagType.NUMBER,
            target = FlagTarget(value = TargetValue(value = RawFlagValue(numberValue = 0.0)))
        )
        assertThat(zeroNumberFlag.asBool()).isTrue()

        val jsonFlag = falsyStringFlag.copy(
            type = FlagType.JSON,
            target = FlagTarget(value = TargetValue(value = RawFlagValue(jsonValue = mapOf("a" to 1))))
        )
        assertThat(jsonFlag.asBool()).isTrue()

        val emptyJsonFlag = falsyStringFlag.copy(
            type = FlagType.JSON,
            target = FlagTarget(value = TargetValue(value = RawFlagValue(jsonValue = emptyMap<String, Any?>())))
        )
        assertThat(emptyJsonFlag.asBool()).isTrue()

        // A boolean-typed flag is the only case where asBool() reflects the actual value.
        val falseBooleanFlag = falsyStringFlag.copy(
            type = FlagType.BOOLEAN,
            target = FlagTarget(value = TargetValue(value = RawFlagValue(booleanValue = false)))
        )
        assertThat(falseBooleanFlag.asBool()).isFalse()
    }

    @Test
    fun asJsonDecodesObjectAndArrayValues() {
        val objectFlag = Flag(
            version = "1",
            type = FlagType.JSON,
            key = "config",
            name = "config",
            target = FlagTarget(value = TargetValue(value = RawFlagValue(jsonValue = mapOf("nested" to true, "limit" to 5)))),
            rules = emptyList(),
            rollout = null
        )

        assertThat(objectFlag.asJson()).isEqualTo(mapOf("nested" to true, "limit" to 5))

        val arrayFlag = objectFlag.copy(
            target = FlagTarget(value = TargetValue(value = RawFlagValue(jsonValue = listOf("a", "b", "c"))))
        )

        assertThat(arrayFlag.asJson()).isEqualTo(listOf("a", "b", "c"))
    }

    @Test
    fun asJsonFallsBackToEmptyMapForNonJsonFlagsAndForNonObjectNonArrayJsonValues() {
        val booleanFlag = Flag(
            version = "1",
            type = FlagType.BOOLEAN,
            key = "feature",
            name = "feature",
            target = FlagTarget(value = TargetValue(value = RawFlagValue(booleanValue = true))),
            rules = emptyList(),
            rollout = null
        )
        assertThat(booleanFlag.asJson()).isEqualTo(emptyMap<String, Any?>())

        val stringFlag = booleanFlag.copy(
            type = FlagType.STRING,
            target = FlagTarget(value = TargetValue(value = RawFlagValue(stringValue = "abc")))
        )
        assertThat(stringFlag.asJson()).isEqualTo(emptyMap<String, Any?>())

        val numberFlag = booleanFlag.copy(
            type = FlagType.NUMBER,
            target = FlagTarget(value = TargetValue(value = RawFlagValue(numberValue = 42.0)))
        )
        assertThat(numberFlag.asJson()).isEqualTo(emptyMap<String, Any?>())

        // A json flag whose decoded value is a bare scalar (allowed on the wire, but not a real
        // object/array shape) also falls back to the safe empty value rather than being returned as-is.
        val scalarJsonFlag = booleanFlag.copy(
            type = FlagType.JSON,
            target = FlagTarget(value = TargetValue(value = RawFlagValue(jsonValue = "not-an-object-or-array")))
        )
        assertThat(scalarJsonFlag.asJson()).isEqualTo(emptyMap<String, Any?>())
    }

    @Test
    fun otherAccessorsFallBackToSafeZeroValuesOnAJsonFlag() {
        val jsonFlag = Flag(
            version = "1",
            type = FlagType.JSON,
            key = "config",
            name = "config",
            target = FlagTarget(value = TargetValue(value = RawFlagValue(jsonValue = mapOf("a" to 1)))),
            rules = emptyList(),
            rollout = null
        )

        assertThat(jsonFlag.isEnabled()).isFalse()
        assertThat(jsonFlag.asBool()).isTrue()
        assertThat(jsonFlag.asString()).isEqualTo("")
        assertThat(jsonFlag.asNumber()).isEqualTo(0.0)
    }
}
