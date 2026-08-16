package com.zenmanage.kotlin.flags

import com.fasterxml.jackson.annotation.JsonProperty

public data class FlagTarget(
    var version: String? = null,
    @field:JsonProperty("expired_at")
    var expiredAt: String? = null,
    @field:JsonProperty("published_at")
    var publishedAt: String? = null,
    @field:JsonProperty("scheduled_at")
    var scheduledAt: String? = null,
    var value: TargetValue? = null
)
