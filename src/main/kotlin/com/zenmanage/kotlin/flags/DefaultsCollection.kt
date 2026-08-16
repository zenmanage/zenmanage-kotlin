package com.zenmanage.kotlin.flags

public class DefaultsCollection {
    private val defaults: LinkedHashMap<String, Any?> = linkedMapOf()

    public fun set(key: String, value: Any?): DefaultsCollection {
        defaults[key] = value
        return this
    }

    public fun has(key: String): Boolean = defaults.containsKey(key)

    public fun get(key: String): Any? = defaults[key]

    public fun delete(key: String): Boolean = defaults.remove(key) != null

    public fun clear(): Unit {
        defaults.clear()
    }

    public fun size(): Int = defaults.size

    public fun keys(): List<String> = defaults.keys.toList()

    public companion object {
        public fun fromMap(values: Map<String, Any?>): DefaultsCollection {
            val collection = DefaultsCollection()
            values.forEach { (key, value) -> collection.set(key, value) }
            return collection
        }
    }
}
