package com.zenmanage.kotlin.flags

public data class FlagData(
    var version: String = "1",
    var type: FlagType = FlagType.STRING,
    var key: String = "",
    var name: String = "",
    var target: FlagTarget? = null,
    var rules: List<Rule>? = null,
    var rollout: RolloutData? = null
)
