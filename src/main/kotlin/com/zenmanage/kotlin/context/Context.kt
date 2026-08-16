package com.zenmanage.kotlin.context

public data class Context(
    val type: String,
    val name: String? = null,
    val identifier: String? = null,
    private val attributes: LinkedHashMap<String, Attribute> = linkedMapOf()
) {
    public fun addAttribute(attribute: Attribute): Context {
        attributes[attribute.key] = attribute
        return this
    }

    public fun hasAttribute(key: String): Boolean = attributes.containsKey(key)

    public fun getAttribute(key: String): Attribute? = attributes[key]

    public fun getAttributes(): List<Attribute> = attributes.values.toList()

    public fun toMap(): Map<String, Any> {
        val result = linkedMapOf<String, Any>("type" to type)
        name?.let { result["name"] = it }
        identifier?.let { result["identifier"] = it }
        if (attributes.isNotEmpty()) {
            result["attributes"] = attributes.values.map(Attribute::toMap)
        }
        return result
    }

    public companion object {
        public fun single(type: String, identifier: String): Context = Context(type = type, identifier = identifier)

        public fun single(type: String, identifier: String, name: String): Context =
            Context(type = type, identifier = identifier, name = name)

        @Suppress("UNCHECKED_CAST")
        public fun fromMap(data: Map<String, Any?>): Context {
            val type = data["type"] as? String ?: "anonymous"
            val name = data["name"] as? String
            val identifier = data["identifier"] as? String

            val context = Context(type = type, name = name, identifier = identifier)
            val rawAttributes = data["attributes"] as? List<Map<String, Any?>> ?: emptyList()

            rawAttributes.forEach { attributeMap ->
                val key = attributeMap["key"] as? String ?: return@forEach
                val attribute = Attribute(key)
                val rawValues = attributeMap["values"] as? List<Map<String, Any?>> ?: emptyList()
                rawValues.forEach { valueMap ->
                    valueMap["value"]?.let { attribute.addValue(it.toString()) }
                }
                context.addAttribute(attribute)
            }

            return context
        }
    }
}
