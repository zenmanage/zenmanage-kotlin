package com.zenmanage.kotlin.flags

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

@JsonDeserialize(using = RuleConditionDeserializer::class)
public data class RuleCondition(
    var attribute: String = "",
    var operator: String = "",
    var value: Any? = null
)

internal class RuleConditionDeserializer : StdDeserializer<RuleCondition>(RuleCondition::class.java) {
    override fun deserialize(p: JsonParser, ctx: DeserializationContext): RuleCondition {
        val node: JsonNode = p.codec.readTree(p)

        return if (node.has("selector") && !node.get("selector").isNull && node.get("selector").asText().isNotEmpty()) {
            // CDN wire format: selector/selector_subtype/comparer/values
            val operator = if (node.has("comparer")) node.get("comparer").asText() else ""
            val selector = node.get("selector").asText()
            val attribute = when (selector) {
                "context", "segment" -> selector
                else -> if (node.has("selector_subtype") && !node.get("selector_subtype").isNull)
                    node.get("selector_subtype").asText() else ""
            }
            val value: Any? = if (node.has("values") && node.get("values").isArray) {
                val values = mutableListOf<Any?>()
                for (v in node.get("values")) {
                    values.add(when {
                        v.isTextual -> v.asText()
                        v.isNumber -> v.numberValue()
                        v.isBoolean -> v.booleanValue()
                        v.isObject -> p.codec.treeToValue(v, Object::class.java)
                        else -> null
                    })
                }
                values
            } else null
            RuleCondition(attribute = attribute, operator = operator, value = value)
        } else {
            // Legacy format: attribute/operator/value
            val attribute = if (node.has("attribute")) node.get("attribute").asText() else ""
            val operator = if (node.has("operator")) node.get("operator").asText() else ""
            val value: Any? = if (node.has("value")) p.codec.treeToValue(node.get("value"), Object::class.java) else null
            RuleCondition(attribute = attribute, operator = operator, value = value)
        }
    }
}
