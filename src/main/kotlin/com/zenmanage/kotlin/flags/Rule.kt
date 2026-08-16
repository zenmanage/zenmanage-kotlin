package com.zenmanage.kotlin.flags

public data class Rule(
    var version: String? = null,
    var description: String? = null,
    var criteria: RuleCondition? = null,
    var clauses: List<RuleCondition>? = null,
    var position: Int? = null,
    var value: RuleValue? = null
)
