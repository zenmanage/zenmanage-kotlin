package com.zenmanage.kotlin.rules

import com.zenmanage.kotlin.context.Attribute
import com.zenmanage.kotlin.context.Context
import com.zenmanage.kotlin.flags.Rule
import com.zenmanage.kotlin.flags.RuleCondition
import com.zenmanage.kotlin.flags.RuleValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RuleEngineTest {
    private val engine = RuleEngine()

    @Test
    fun evaluatesEqualsCondition() {
        val context = Context.single("user", "u-1")
            .addAttribute(Attribute("country", listOf("US")))

        val rule = Rule(
            criteria = RuleCondition(attribute = "country", operator = "equal", value = "US"),
            value = RuleValue()
        )

        val result = engine.evaluate(listOf(rule), context)
        assertThat(result).isNotNull
    }

    @Test
    fun evaluatesNumericComparison() {
        val context = Context.single("user", "u-1")
            .addAttribute(Attribute("spend", listOf("120")))

        val rule = Rule(
            criteria = RuleCondition(attribute = "spend", operator = "gt", value = "100"),
            value = RuleValue()
        )

        val result = engine.evaluate(listOf(rule), context)
        assertThat(result).isNotNull
    }

    @Test
    fun evaluatesContextRuleWithIdentifierList() {
        val context = Context.single("user", "u-2")
        val rule = Rule(
            criteria = RuleCondition(attribute = "context", operator = "in", value = listOf("u-1", "u-2")),
            value = RuleValue()
        )

        val result = engine.evaluate(listOf(rule), context)
        assertThat(result).isNotNull
    }

    @Test
    fun returnsNullWhenNoRulesMatch() {
        val context = Context.single("user", "u-1")
            .addAttribute(Attribute("country", listOf("CA")))

        val rule = Rule(criteria = RuleCondition(attribute = "country", operator = "equal", value = "US"))
        val result = engine.evaluate(listOf(rule), context)
        assertThat(result).isNull()
    }

    @Test
    fun evaluatesContainsCondition() {
        val context = Context.single("user", "u-1")
            .addAttribute(Attribute("email", listOf("alice@example.com")))

        val rule = Rule(
            criteria = RuleCondition(attribute = "email", operator = "contains", value = "example.com"),
            value = RuleValue()
        )

        val result = engine.evaluate(listOf(rule), context)
        assertThat(result).isNotNull
    }

    @Test
    fun evaluatesNotContainsCondition() {
        val context = Context.single("user", "u-1")
            .addAttribute(Attribute("email", listOf("alice@example.com")))

        val rule = Rule(
            criteria = RuleCondition(attribute = "email", operator = "notcontains", value = "other.com"),
            value = RuleValue()
        )

        val result = engine.evaluate(listOf(rule), context)
        assertThat(result).isNotNull
    }

    @Test
    fun evaluatesNotStartsWithCondition() {
        val context = Context.single("user", "u-1")
            .addAttribute(Attribute("email", listOf("alice@example.com")))

        val rule = Rule(
            criteria = RuleCondition(attribute = "email", operator = "notstartswith", value = "admin"),
            value = RuleValue()
        )

        val result = engine.evaluate(listOf(rule), context)
        assertThat(result).isNotNull
    }

    @Test
    fun evaluatesNotEndsWithCondition() {
        val context = Context.single("user", "u-1")
            .addAttribute(Attribute("email", listOf("alice@example.com")))

        val rule = Rule(
            criteria = RuleCondition(attribute = "email", operator = "notendswith", value = ".org"),
            value = RuleValue()
        )

        val result = engine.evaluate(listOf(rule), context)
        assertThat(result).isNotNull
    }

    @Test
    fun isNullMatchesWhenAttributeIsAbsent() {
        val context = Context.single("user", "u-1")
        val rule = Rule(criteria = RuleCondition(attribute = "tag", operator = "isnull", value = null), value = RuleValue())
        assertThat(engine.evaluate(listOf(rule), context)).isNotNull
    }

    @Test
    fun isNullMatchesWhenAttributeValueIsEmpty() {
        val context = Context.single("user", "u-1").addAttribute(Attribute("tag", listOf("")))
        val rule = Rule(criteria = RuleCondition(attribute = "tag", operator = "isnull", value = null), value = RuleValue())
        assertThat(engine.evaluate(listOf(rule), context)).isNotNull
    }

    @Test
    fun isNullDoesNotMatchWhenAttributeHasValue() {
        val context = Context.single("user", "u-1").addAttribute(Attribute("tag", listOf("active")))
        val rule = Rule(criteria = RuleCondition(attribute = "tag", operator = "isnull", value = null), value = RuleValue())
        assertThat(engine.evaluate(listOf(rule), context)).isNull()
    }

    @Test
    fun notNullMatchesWhenAttributeHasValue() {
        val context = Context.single("user", "u-1").addAttribute(Attribute("tag", listOf("active")))
        val rule = Rule(criteria = RuleCondition(attribute = "tag", operator = "notnull", value = null), value = RuleValue())
        assertThat(engine.evaluate(listOf(rule), context)).isNotNull
    }

    @Test
    fun notNullDoesNotMatchWhenAttributeIsAbsent() {
        val context = Context.single("user", "u-1")
        val rule = Rule(criteria = RuleCondition(attribute = "tag", operator = "notnull", value = null), value = RuleValue())
        assertThat(engine.evaluate(listOf(rule), context)).isNull()
    }

    @Test
    fun negativeOperatorsMatchWhenAttributeIsAbsent() {
        val context = Context.single("user", "u-1")
        listOf("notequal", "notin", "notcontains", "notstartswith", "notendswith").forEach { op ->
            val rule = Rule(criteria = RuleCondition(attribute = "missing", operator = op, value = "x"), value = RuleValue())
            assertThat(engine.evaluate(listOf(rule), context)).withFailMessage("$op should match absent attr").isNotNull
        }
    }
}
