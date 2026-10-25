package com.zenmanage.kotlin.flags

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zenmanage.kotlin.context.Attribute
import com.zenmanage.kotlin.context.Context
import com.zenmanage.kotlin.rules.RuleEngine
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * The CDN ships conditions as `selector` / `selector_subtype` / `comparer` / `values`. The legacy
 * `attribute` / `operator` / `value` shape is still accepted. Both must evaluate the same way.
 */
class RuleConditionTest {
    private val mapper = jacksonObjectMapper()
    private val engine = RuleEngine()

    private fun conditionFrom(json: String): RuleCondition = mapper.readValue(json, RuleCondition::class.java)

    private fun matches(condition: RuleCondition, context: Context): Boolean =
        engine.evaluate(listOf(Rule(criteria = condition, value = RuleValue())), context) != null

    @Test
    fun readsAnAttributeSelectorFromTheCdnShape() {
        val condition = conditionFrom(
            """{"selector":"attribute","selector_subtype":"country","comparer":"equal","values":["US"]}"""
        )

        assertThat(condition.attribute).isEqualTo("country")
        assertThat(condition.operator).isEqualTo("equal")
        assertThat(matches(condition, Context.single("user", "u1").addAttribute(Attribute("country", listOf("US"))))).isTrue()
        assertThat(matches(condition, Context.single("user", "u1").addAttribute(Attribute("country", listOf("CA"))))).isFalse()
    }

    @Test
    fun mapsTheContextSelectorToTheIdentifierCheck() {
        val condition = conditionFrom(
            """{"selector":"context","selector_subtype":null,"comparer":"equal","values":["user-123"]}"""
        )

        assertThat(condition.attribute).isEqualTo("context")
        assertThat(matches(condition, Context.single("user", "user-123"))).isTrue()
        assertThat(matches(condition, Context.single("user", "user-999"))).isFalse()
    }

    @Test
    fun legacyAttributeShapeStillEvaluates() {
        val condition = conditionFrom("""{"attribute":"plan","operator":"in","value":["pro","team"]}""")

        assertThat(matches(condition, Context.single("user", "u1").addAttribute(Attribute("plan", listOf("pro"))))).isTrue()
    }

    @Test
    fun cdnNumericComparisonUsesTheNumericOperator() {
        val condition = conditionFrom(
            """{"selector":"attribute","selector_subtype":"tier","comparer":"gte","values":["3"]}"""
        )

        assertThat(matches(condition, Context.single("user", "u1").addAttribute(Attribute("tier", listOf("3"))))).isTrue()
        assertThat(matches(condition, Context.single("user", "u1").addAttribute(Attribute("tier", listOf("1"))))).isFalse()
    }
}
