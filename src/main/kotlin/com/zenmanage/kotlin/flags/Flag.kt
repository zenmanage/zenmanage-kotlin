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
