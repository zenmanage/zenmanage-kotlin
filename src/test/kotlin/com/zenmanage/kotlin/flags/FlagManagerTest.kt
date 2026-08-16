package com.zenmanage.kotlin.flags

import com.zenmanage.kotlin.api.ApiClient
import com.zenmanage.kotlin.cache.InMemoryCache
import com.zenmanage.kotlin.config.NullLogger
import com.zenmanage.kotlin.context.Context
import com.zenmanage.kotlin.errors.EvaluationException
import com.zenmanage.kotlin.rules.RuleEngine
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class FlagManagerTest {
    @Test
    fun returnsFlagFromApiAndCachesResponse() {
        val manager = FlagManager(
            apiClient = FakeApiClient(response = rulesWithBooleanFlag()),
            cache = InMemoryCache(),
            ruleEngine = RuleEngine(),
            cacheTtlSeconds = 60,
            logger = NullLogger()
        )

        val flag = manager.single("new-dashboard", false)
        assertThat(flag.isEnabled()).isTrue()
    }

    @Test
    fun appliesDefaultsAndInlineFallbacks() {
        val manager = FlagManager(
            apiClient = FakeApiClient(response = RulesResponse(version = "1", flags = emptyList())),
            cache = InMemoryCache(),
            ruleEngine = RuleEngine(),
            cacheTtlSeconds = 60,
            logger = NullLogger()
        )

        val defaults = DefaultsCollection().set("missing", "fallback")
        val fromDefaults = manager.withDefaults(defaults).single("missing").asString()
        assertThat(fromDefaults).isEqualTo("fallback")

        val inline = manager.single("missing-inline", 123).asNumber()
        assertThat(inline).isEqualTo(123.0)
    }

    @Test
    fun reportsUsageWithInlineDefaultValueWhenFlagMissing() {
        val apiClient = FakeApiClient(response = RulesResponse(version = "1", flags = emptyList()))
        val manager = FlagManager(
            apiClient = apiClient,
            cache = InMemoryCache(),
            ruleEngine = RuleEngine(),
            cacheTtlSeconds = 60,
            logger = NullLogger()
        )

        manager.single("missing", true)

        assertThat(apiClient.lastReportedKey).isEqualTo("missing")
        assertThat(apiClient.lastDefaultValue).isEqualTo(true)
    }

    @Test
    fun reportsUsageWithDefaultsCollectionValueWhenFlagMissing() {
        val apiClient = FakeApiClient(response = RulesResponse(version = "1", flags = emptyList()))
        val manager = FlagManager(
            apiClient = apiClient,
            cache = InMemoryCache(),
            ruleEngine = RuleEngine(),
            cacheTtlSeconds = 60,
            logger = NullLogger()
        )

        val defaults = DefaultsCollection().set("missing", "fallback")
        manager.withDefaults(defaults).single("missing")

        assertThat(apiClient.lastReportedKey).isEqualTo("missing")
        assertThat(apiClient.lastDefaultValue).isEqualTo("fallback")
    }

    @Test
    fun reportsUsageWithoutDefaultValueWhenFlagFound() {
        val apiClient = FakeApiClient(response = rulesWithBooleanFlag())
        val manager = FlagManager(
            apiClient = apiClient,
            cache = InMemoryCache(),
            ruleEngine = RuleEngine(),
            cacheTtlSeconds = 60,
            logger = NullLogger()
        )

        manager.single("new-dashboard")

        assertThat(apiClient.lastReportedKey).isEqualTo("new-dashboard")
        assertThat(apiClient.lastDefaultValue).isNull()
    }

    @Test
    fun reportsUsageWithInlineDefaultValueWhenFlagFound() {
        val apiClient = FakeApiClient(response = rulesWithBooleanFlag())
        val manager = FlagManager(
            apiClient = apiClient,
            cache = InMemoryCache(),
            ruleEngine = RuleEngine(),
            cacheTtlSeconds = 60,
            logger = NullLogger()
        )

        manager.single("new-dashboard", false)

        assertThat(apiClient.lastReportedKey).isEqualTo("new-dashboard")
        assertThat(apiClient.lastDefaultValue).isEqualTo(false)
    }

    @Test
    fun reportsUsageWithDefaultsCollectionValueWhenFlagFound() {
        val apiClient = FakeApiClient(response = rulesWithBooleanFlag())
        val manager = FlagManager(
            apiClient = apiClient,
            cache = InMemoryCache(),
            ruleEngine = RuleEngine(),
            cacheTtlSeconds = 60,
            logger = NullLogger()
        )

        val defaults = DefaultsCollection().set("new-dashboard", true)
        manager.withDefaults(defaults).single("new-dashboard")

        assertThat(apiClient.lastReportedKey).isEqualTo("new-dashboard")
        assertThat(apiClient.lastDefaultValue).isEqualTo(true)
    }

    @Test
    fun throwsWhenFlagMissingAndNoDefaults() {
        val manager = FlagManager(
            apiClient = FakeApiClient(response = RulesResponse(version = "1", flags = emptyList())),
            cache = InMemoryCache(),
            ruleEngine = RuleEngine(),
            cacheTtlSeconds = 60,
            logger = NullLogger()
        )

        assertThrows(EvaluationException::class.java) {
            manager.single("does-not-exist")
        }
    }

    @Test
    fun evaluatesRuleAgainstContext() {
        val rule = Rule(
            criteria = RuleCondition(attribute = "country", operator = "equal", value = "US"),
            value = RuleValue(value = RawFlagValue(booleanValue = true))
        )

        val flag = FlagData(
            version = "1",
            type = FlagType.BOOLEAN,
            key = "geo-flag",
            name = "geo-flag",
            target = FlagTarget(value = TargetValue(value = RawFlagValue(booleanValue = false))),
            rules = listOf(rule)
        )

        val manager = FlagManager(
            apiClient = FakeApiClient(response = RulesResponse(version = "1", flags = listOf(flag))),
            cache = InMemoryCache(),
            ruleEngine = RuleEngine(),
            cacheTtlSeconds = 60,
            logger = NullLogger()
        )

        val context = Context.single("user", "u-1").addAttribute(com.zenmanage.kotlin.context.Attribute("country", listOf("US")))
        val evaluated = manager.withContext(context).single("geo-flag")
        assertThat(evaluated.isEnabled()).isTrue()
    }

    @Test
    fun rolloutUsesDeterministicBucket() {
        val rolloutFlag = FlagData(
            version = "1",
            type = FlagType.BOOLEAN,
            key = "rollout-flag",
            name = "rollout-flag",
            target = FlagTarget(value = TargetValue(value = RawFlagValue(booleanValue = false))),
            rollout = RolloutData(
                percentage = 100.0,
                salt = "salt-1",
                target = FlagTarget(value = TargetValue(value = RawFlagValue(booleanValue = true))),
                rules = emptyList()
            )
        )

        val manager = FlagManager(
            apiClient = FakeApiClient(response = RulesResponse(version = "1", flags = listOf(rolloutFlag))),
            cache = InMemoryCache(),
            ruleEngine = RuleEngine(),
            cacheTtlSeconds = 60,
            logger = NullLogger()
        )

        val context = Context.single("user", "stable-user")
        val first = manager.withContext(context).single("rollout-flag").isEnabled()
        val second = manager.withContext(context).single("rollout-flag").isEnabled()

        assertThat(first).isTrue()
        assertThat(second).isEqualTo(first)
    }

    private fun rulesWithBooleanFlag(): RulesResponse {
        return RulesResponse(
            version = "1",
            flags = listOf(
                FlagData(
                    version = "1",
                    type = FlagType.BOOLEAN,
                    key = "new-dashboard",
                    name = "new-dashboard",
                    target = FlagTarget(value = TargetValue(value = RawFlagValue(booleanValue = true))),
                    rules = emptyList()
                )
            )
        )
    }

    private class FakeApiClient(private val response: RulesResponse) : ApiClient(
        environmentToken = "mob_test",
        apiEndpoint = "https://example.com",
        logger = NullLogger(),
        usageReportingEnabled = false,
        sdkVersion = "test",
        clientAgent = "zenmanage-kotlin-test"
    ) {
        var lastReportedKey: String? = null
        var lastDefaultValue: Any? = null

        override fun getRules(): RulesResponse = response

        override fun reportUsage(key: String, context: Context?, defaultValue: Any?): Unit {
            lastReportedKey = key
            lastDefaultValue = defaultValue
        }
    }
}
