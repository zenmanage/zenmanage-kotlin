package com.zenmanage.kotlin.rules

import com.zenmanage.kotlin.context.Attribute
import com.zenmanage.kotlin.context.Context
import com.zenmanage.kotlin.flags.Rule
import com.zenmanage.kotlin.flags.RuleCondition
import com.zenmanage.kotlin.flags.RuleValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Rules are evaluated lowest `position` first (first match wins). The payload isn't guaranteed to
 * arrive in that order, so the engine orders the rules itself.
 */
class RulePositionTest {
    private val engine = RuleEngine()
    private val usContext = Context.single("user", "u1").addAttribute(Attribute("country", listOf("US")))

    private fun rule(label: String, position: Int?, country: String = "US") = Rule(
        version = label,
        clauses = listOf(RuleCondition(attribute = "country", operator = "equal", value = country)),
        position = position,
        value = RuleValue()
    )

    @Test
    fun keepsPayloadOrderWhenNoRuleHasAPosition() {
        val rules = listOf(rule("b", null), rule("a", null), rule("c", null))

        assertThat(engine.evaluate(rules, usContext)?.version).isEqualTo("b")
    }

    @Test
    fun returnsTheLowestPositionMatchEvenWhenItArrivesLast() {
        val rules = listOf(rule("late", 2), rule("early", 1))

        assertThat(engine.evaluate(rules, usContext)?.version).isEqualTo("early")
    }

    @Test
    fun skipsAnEarlierPositionThatDoesNotMatch() {
        val rules = listOf(rule("ca-first", 1, country = "CA"), rule("us-second", 2))

        assertThat(engine.evaluate(rules, usContext)?.version).isEqualTo("us-second")
    }

    @Test
    fun treatsAMissingPositionAsZero() {
        val rules = listOf(rule("positioned", 1), rule("unpositioned", null))

        assertThat(engine.evaluate(rules, usContext)?.version).isEqualTo("unpositioned")
    }

    @Test
    fun keepsPayloadOrderForRulesSharingAPosition() {
        val rules = listOf(rule("x", 2), rule("y", 1), rule("z", 1))

        assertThat(engine.evaluate(rules, usContext)?.version).isEqualTo("y")
    }

    @Test
    fun returnsNullWhenNoRuleMatches() {
        assertThat(engine.evaluate(emptyList(), usContext)).isNull()
    }
}
