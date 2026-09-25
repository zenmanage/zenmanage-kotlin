package com.zenmanage.kotlin.flags

import com.fasterxml.jackson.annotation.JsonProperty

public data class RawFlagValue(
    @field:JsonProperty("boolean")
    var booleanValue: Boolean? = null,
    @field:JsonProperty("string")
    var stringValue: String? = null,
    @field:JsonProperty("number")
    var numberValue: Double? = null,
    /**
     * The decoded value for a `json`-typed flag. Jackson deserializes a JSON object into a
     * `Map<String, Any?>` and a JSON array into a `List<Any?>` here, matching how [RuleCondition]
     * already handles arbitrary structured values elsewhere in this SDK. Deliberately excluded
     * from [toKotlinValue]'s fallback chain — [com.zenmanage.kotlin.flags.Flag.asJson] reads it
     * directly so that [com.zenmanage.kotlin.flags.Flag.asString]/[com.zenmanage.kotlin.flags.Flag.asNumber]
     * keep returning their documented safe zero value for `json`-typed flags instead of a stringified map.
     */
    @field:JsonProperty("json")
    var jsonValue: Any? = null
) {
    public fun toKotlinValue(): Any {
        return booleanValue ?: stringValue ?: numberValue ?: ""
    }

    public companion object {
        public fun fromKotlinValue(value: Any?): RawFlagValue {
            return when (value) {
                is Boolean -> RawFlagValue(booleanValue = value)
                is Number -> RawFlagValue(numberValue = value.toDouble())
                is Map<*, *> -> RawFlagValue(jsonValue = value)
                is List<*> -> RawFlagValue(jsonValue = value)
                null -> RawFlagValue(stringValue = "")
                else -> RawFlagValue(stringValue = value.toString())
            }
        }
    }
}
