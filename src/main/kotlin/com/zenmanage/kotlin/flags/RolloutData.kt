package com.zenmanage.kotlin.flags

public data class RolloutData(
    var target: FlagTarget? = null,
    var rules: List<Rule>? = null,
    var percentage: Double = 0.0,
    var salt: String = "",
    var status: String? = null
)
