package com.zenmanage.kotlin.flags

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

public enum class FlagType(@get:JsonValue val wireValue: String) {
    BOOLEAN("boolean"),
    STRING("string"),
    NUMBER("number");

    public companion object {
        @JvmStatic
        @JsonCreator
        public fun fromWireValue(value: String): FlagType {
            return entries.firstOrNull { it.wireValue.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unsupported flag type: $value")
        }
    }
}
