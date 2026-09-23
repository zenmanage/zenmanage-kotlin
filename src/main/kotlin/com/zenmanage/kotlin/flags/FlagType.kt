package com.zenmanage.kotlin.flags

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

public enum class FlagType(@get:JsonValue val wireValue: String) {
    BOOLEAN("boolean"),
    STRING("string"),
    NUMBER("number"),

    /**
     * Sentinel for any flag type not yet recognized by this SDK release (for example, a
     * future "json" flag type). Flags parsed with this type are treated like a missing flag
     * during evaluation: they fall back to the caller's default rather than throwing, so a
     * single unrecognized flag in a rules payload never breaks parsing of the whole payload
     * or evaluation of the other flags in it.
     */
    UNKNOWN("unknown");

    public companion object {
        @JvmStatic
        @JsonCreator
        public fun fromWireValue(value: String): FlagType {
            return entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
