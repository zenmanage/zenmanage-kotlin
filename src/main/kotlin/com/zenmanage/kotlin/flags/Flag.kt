package com.zenmanage.kotlin.flags

public data class Flag(
    val version: String,
    val type: FlagType,
    val key: String,
    val name: String,
    val target: FlagTarget?,
    val rules: List<Rule>,
    val rollout: RolloutData?
) {
    public fun isEnabled(): Boolean {
        if (type != FlagType.BOOLEAN) {
            return false
        }
        return getValue() as? Boolean ?: false
    }

    public fun asBool(): Boolean {
        return when (val value = getValue()) {
            is Boolean -> value
            is Number -> value.toDouble() != 0.0
            else -> value.toString().toBooleanStrictOrNull() ?: false
        }
    }

    public fun asString(): String = getValue().toString()

    public fun asNumber(): Double {
        return when (val value = getValue()) {
            is Number -> value.toDouble()
            is Boolean -> if (value) 1.0 else 0.0
            else -> value.toString().toDoubleOrNull() ?: 0.0
        }
    }

    /**
     * Returns the decoded value of a `json`-typed flag. Both JSON objects and JSON arrays are
     * valid decoded shapes here: an object decodes to a `Map<String, Any?>`, an array decodes to
     * a `List<Any?>`, matching how the rest of the SDK already parses structured API values.
     *
     * Only recognizes this flag's own `json` value wrapper. Called on any other flag type — or on
     * a `json` flag whose decoded value isn't itself an object or array (a bare JSON scalar) — this
     * falls back to an empty map, the same "safe zero value" behavior `asString()`/`asNumber()` use
     * for their own types, rather than attempting a lossy conversion.
     */
    public fun asJson(): Any {
        return when (val value = target?.value?.value?.jsonValue) {
            is Map<*, *> -> value
            is List<*> -> value
            else -> emptyMap<String, Any?>()
        }
    }

    public fun getValue(): Any {
        return target?.value?.value?.toKotlinValue() ?: ""
    }

    public companion object {
        public fun fromData(data: FlagData): Flag {
            return Flag(
                version = data.version,
                type = data.type,
                key = data.key,
                name = data.name,
                target = data.target,
                rules = data.rules ?: emptyList(),
                rollout = data.rollout
            )
        }
    }
}
