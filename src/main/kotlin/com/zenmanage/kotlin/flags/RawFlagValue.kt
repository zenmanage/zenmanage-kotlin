package com.zenmanage.kotlin.flags

import com.fasterxml.jackson.annotation.JsonProperty

public data class RawFlagValue(
    @field:JsonProperty("boolean")
    var booleanValue: Boolean? = null,
    @field:JsonProperty("string")
    var stringValue: String? = null,
    @field:JsonProperty("number")
    var numberValue: Double? = null
) {
    public fun toKotlinValue(): Any {
        return booleanValue ?: stringValue ?: numberValue ?: ""
    }

    public companion object {
        public fun fromKotlinValue(value: Any?): RawFlagValue {
            return when (value) {
                is Boolean -> RawFlagValue(booleanValue = value)
                is Number -> RawFlagValue(numberValue = value.toDouble())
                null -> RawFlagValue(stringValue = "")
                else -> RawFlagValue(stringValue = value.toString())
            }
        }
    }
}
