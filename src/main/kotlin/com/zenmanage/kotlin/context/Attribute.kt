package com.zenmanage.kotlin.context

public class Attribute(
    public val key: String,
    values: List<String> = emptyList()
) {
    private val mutableValues: MutableList<String> = values.toMutableList()

    public val values: List<String>
        get() = mutableValues.toList()

    public fun addValue(value: String): Attribute {
        mutableValues.add(value)
        return this
    }

    public fun toMap(): Map<String, Any> = mapOf(
        "key" to key,
        "values" to mutableValues.map { mapOf("value" to it) }
    )
}
