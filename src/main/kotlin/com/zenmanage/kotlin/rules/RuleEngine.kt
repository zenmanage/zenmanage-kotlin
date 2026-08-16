package com.zenmanage.kotlin.rules

import com.zenmanage.kotlin.context.Context
import com.zenmanage.kotlin.flags.Rule
import com.zenmanage.kotlin.flags.RuleCondition

public class RuleEngine {
    public fun evaluate(rules: List<Rule>, context: Context): Rule? {
        return rules.firstOrNull { evaluateRule(it, context) }
    }

    private fun evaluateRule(rule: Rule, context: Context): Boolean {
        val clauses = rule.clauses
        if (!clauses.isNullOrEmpty()) {
            return clauses.all { evaluateClause(it, context) }
        }

        rule.criteria?.let { return evaluateClause(it, context) }
        return true
    }

    @Suppress("UNCHECKED_CAST")
    private fun evaluateClause(clause: RuleCondition, context: Context): Boolean {
        if (clause.attribute == "context" || clause.attribute == "segment") {
            return evaluateContextClause(clause, context)
        }

        val attribute = context.getAttribute(clause.attribute)
        if (attribute == null) {
            return clause.operator in setOf("isnull", "notequal", "notin", "notcontains", "notstartswith", "notendswith")
        }
        val attributeValues = attribute.values
        if (clause.operator == "isnull") return attributeValues.all { it.isEmpty() }
        if (clause.operator == "notnull") return attributeValues.any { it.isNotEmpty() }
        val clauseValues = toStringList(clause.value)
        return evaluateByOperator(clause.operator, attributeValues, clauseValues)
    }

    @Suppress("UNCHECKED_CAST")
    private fun evaluateContextClause(clause: RuleCondition, context: Context): Boolean {
        val identifier = context.identifier?.takeIf { it.isNotBlank() } ?: return false
        val matchingIdentifiers = mutableListOf<String>()

        when (val value = clause.value) {
            is String -> matchingIdentifiers += value
            is List<*> -> {
                value.forEach { item ->
                    when (item) {
                        is String -> matchingIdentifiers += item
                        is Map<*, *> -> {
                            val targetId = item["identifier"]?.toString()
                            val targetType = item["type"]?.toString()
                            if (targetId != null && (targetType == null || targetType == context.type)) {
                                matchingIdentifiers += targetId
                            }
                        }
                    }
                }
            }

            is Map<*, *> -> {
                val targetId = value["identifier"]?.toString()
                val targetType = value["type"]?.toString()
                if (targetId != null && (targetType == null || targetType == context.type)) {
                    matchingIdentifiers += targetId
                }
            }
        }

        if (matchingIdentifiers.isEmpty()) {
            return false
        }

        return evaluateByOperator(clause.operator, listOf(identifier), matchingIdentifiers)
    }

    private fun evaluateByOperator(operator: String, values: List<String>, clauseValues: List<String>): Boolean {
        return when (operator) {
            "equal" -> evaluateEquals(values, clauseValues)
            "notequal" -> !evaluateEquals(values, clauseValues)
            "contains" -> evaluateContains(values, clauseValues)
            "notcontains" -> !evaluateContains(values, clauseValues)
            "in" -> evaluateIn(values, clauseValues)
            "notin" -> !evaluateIn(values, clauseValues)
            "startswith" -> evaluateStartsWith(values, clauseValues)
            "notstartswith" -> !evaluateStartsWith(values, clauseValues)
            "endswith" -> evaluateEndsWith(values, clauseValues)
            "notendswith" -> !evaluateEndsWith(values, clauseValues)
            "gt" -> evaluateGreaterThan(values, clauseValues)
            "gte" -> evaluateGreaterThanOrEqual(values, clauseValues)
            "lt" -> evaluateLessThan(values, clauseValues)
            "lte" -> evaluateLessThanOrEqual(values, clauseValues)
            else -> false
        }
    }

    private fun toStringList(value: Any?): List<String> {
        return when (value) {
            null -> emptyList()
            is String -> listOf(value)
            is List<*> -> value.filterIsInstance<String>()
            else -> emptyList()
        }
    }

    private fun evaluateEquals(values: List<String>, targets: List<String>): Boolean {
        val target = targets.firstOrNull() ?: return false
        return values.any { it == target }
    }

    private fun evaluateContains(values: List<String>, targets: List<String>): Boolean {
        val target = targets.firstOrNull() ?: return false
        return values.any { it.contains(target) }
    }

    private fun evaluateIn(values: List<String>, targets: List<String>): Boolean {
        return values.any { it in targets }
    }

    private fun evaluateStartsWith(values: List<String>, targets: List<String>): Boolean {
        val target = targets.firstOrNull() ?: return false
        return values.any { it.startsWith(target) }
    }

    private fun evaluateEndsWith(values: List<String>, targets: List<String>): Boolean {
        val target = targets.firstOrNull() ?: return false
        return values.any { it.endsWith(target) }
    }

    private fun evaluateGreaterThan(values: List<String>, targets: List<String>): Boolean {
        val target = targets.firstOrNull()?.toDoubleOrNull() ?: return false
        return values.any { (it.toDoubleOrNull() ?: Double.NEGATIVE_INFINITY) > target }
    }

    private fun evaluateGreaterThanOrEqual(values: List<String>, targets: List<String>): Boolean {
        val target = targets.firstOrNull()?.toDoubleOrNull() ?: return false
        return values.any { (it.toDoubleOrNull() ?: Double.NEGATIVE_INFINITY) >= target }
    }

    private fun evaluateLessThan(values: List<String>, targets: List<String>): Boolean {
        val target = targets.firstOrNull()?.toDoubleOrNull() ?: return false
        return values.any { (it.toDoubleOrNull() ?: Double.POSITIVE_INFINITY) < target }
    }

    private fun evaluateLessThanOrEqual(values: List<String>, targets: List<String>): Boolean {
        val target = targets.firstOrNull()?.toDoubleOrNull() ?: return false
        return values.any { (it.toDoubleOrNull() ?: Double.POSITIVE_INFINITY) <= target }
    }
}
