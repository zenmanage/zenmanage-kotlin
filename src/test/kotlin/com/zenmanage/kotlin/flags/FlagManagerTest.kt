package com.zenmanage.kotlin.flags

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zenmanage.kotlin.api.ApiClient
import com.zenmanage.kotlin.cache.InMemoryCache
import com.zenmanage.kotlin.config.Logger
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

    @Test
    fun parsesUnrecognizedTypedFlagAlongsideOthersWithoutThrowingAndFallsBackToDefault() {
        val logger = RecordingLogger()
        val manager = FlagManager(
            apiClient = JsonRulesApiClient(rulesPayloadWithUnrecognizedTypeFlag()),
            cache = InMemoryCache(),
            ruleEngine = RuleEngine(),
            cacheTtlSeconds = 60,
            logger = logger
        )

        // The other flags in the same payload still evaluate correctly.
        assertThat(manager.single("bool-flag", false).isEnabled()).isTrue()
        assertThat(manager.single("string-flag", "unused-default").asString()).isEqualTo("hello")
        assertThat(manager.single("number-flag", 0).asNumber()).isEqualTo(42.0)

        // The unrecognized-typed flag resolves to the caller's default rather than throwing.
        val futureFlag = manager.single("future-flag", "fallback-default")
        assertThat(futureFlag.asString()).isEqualTo("fallback-default")

        // It also degrades via a DefaultsCollection fallback, not just an inline default.
        val defaults = DefaultsCollection().set("future-flag", "collection-fallback")
        assertThat(manager.withDefaults(defaults).single("future-flag").asString()).isEqualTo("collection-fallback")

        // all() silently excludes the unknown-type flag rather than failing the whole call.
        assertThat(manager.all().map { it.key }).containsExactlyInAnyOrder("bool-flag", "string-flag", "number-flag")

        // The unrecognized type is logged once rather than swallowed silently.
        assertThat(logger.warnings).anyMatch { it.contains("future-flag") }
    }

    @Test
    fun unrecognizedTypedFlagWithNoDefaultBehavesLikeAMissingFlag() {
        val manager = FlagManager(
            apiClient = JsonRulesApiClient(rulesPayloadWithUnrecognizedTypeFlag()),
            cache = InMemoryCache(),
            ruleEngine = RuleEngine(),
            cacheTtlSeconds = 60,
            logger = NullLogger()
        )

        assertThrows(EvaluationException::class.java) {
            manager.single("future-flag")
        }
    }

    @Test
    fun parsesRulesPayloadWithJsonFlagAlongsideOtherTypesAndEvaluatesAllOfThemNormally() {
        val manager = FlagManager(
            apiClient = JsonRulesApiClient(rulesPayloadWithJsonFlag()),
            cache = InMemoryCache(),
            ruleEngine = RuleEngine(),
            cacheTtlSeconds = 60,
            logger = NullLogger()
        )

        // The json flag evaluates like any other known type: no default fallback, real decoded value.
        val jsonFlag = manager.single("json-flag", "unused-default")
        assertThat(jsonFlag.asJson()).isEqualTo(mapOf("nested" to true, "count" to 3.5))
        assertThat(jsonFlag.asString()).isEqualTo("")
        assertThat(jsonFlag.asNumber()).isEqualTo(0.0)

        // The other flags in the same payload are unaffected by the json flag's presence.
        assertThat(manager.single("bool-flag", false).isEnabled()).isTrue()
        assertThat(manager.single("string-flag", "unused-default").asString()).isEqualTo("hello")
        assertThat(manager.single("number-flag", 0).asNumber()).isEqualTo(42.0)

        // all() now includes the json flag alongside the other recognized types.
        assertThat(manager.all().map { it.key })
            .containsExactlyInAnyOrder("bool-flag", "string-flag", "number-flag", "json-flag")
    }

    @Test
    fun mapAndListDefaultsAreTypedAsJsonRatherThanStringified() {
        val manager = FlagManager(
            apiClient = FakeApiClient(response = RulesResponse(version = "1", flags = emptyList())),
            cache = InMemoryCache(),
            ruleEngine = RuleEngine(),
            cacheTtlSeconds = 60,
            logger = NullLogger()
        )

        val mapDefaultFlag = manager.single("missing-map", mapOf("enabled" to true, "limit" to 5.0))
        assertThat(mapDefaultFlag.type).isEqualTo(FlagType.JSON)
        assertThat(mapDefaultFlag.asJson()).isEqualTo(mapOf("enabled" to true, "limit" to 5.0))
        assertThat(mapDefaultFlag.asString()).isEqualTo("")

        val listDefaultFlag = manager.single("missing-list", listOf(1.0, 2.0, 3.0))
        assertThat(listDefaultFlag.type).isEqualTo(FlagType.JSON)
        assertThat(listDefaultFlag.asJson()).isEqualTo(listOf(1.0, 2.0, 3.0))

        // Also works via a DefaultsCollection, not just an inline default.
        val defaults = DefaultsCollection().set("missing-from-collection", mapOf("a" to 1.0))
        val fromCollection = manager.withDefaults(defaults).single("missing-from-collection")
        assertThat(fromCollection.type).isEqualTo(FlagType.JSON)
        assertThat(fromCollection.asJson()).isEqualTo(mapOf("a" to 1.0))
    }

    private fun rulesPayloadWithUnrecognizedTypeFlag(): String {
        return """
            {
              "version": "1",
              "flags": [
                {
                  "version": "1",
                  "type": "boolean",
                  "key": "bool-flag",
                  "name": "bool-flag",
                  "target": { "value": { "value": { "boolean": true } } },
                  "rules": []
                },
                {
                  "version": "1",
                  "type": "string",
                  "key": "string-flag",
                  "name": "string-flag",
                  "target": { "value": { "value": { "string": "hello" } } },
                  "rules": []
                },
                {
                  "version": "1",
                  "type": "number",
                  "key": "number-flag",
                  "name": "number-flag",
                  "target": { "value": { "value": { "number": 42 } } },
                  "rules": []
                },
                {
                  "version": "1",
                  "type": "some-future-type",
                  "key": "future-flag",
                  "name": "future-flag",
                  "target": { "value": { "value": { "future-type": "whatever" } } },
                  "rules": []
                }
              ]
            }
        """.trimIndent()
    }

    private fun rulesPayloadWithJsonFlag(): String {
        return """
            {
              "version": "1",
              "flags": [
                {
                  "version": "1",
                  "type": "boolean",
                  "key": "bool-flag",
                  "name": "bool-flag",
                  "target": { "value": { "value": { "boolean": true } } },
                  "rules": []
                },
                {
                  "version": "1",
                  "type": "string",
                  "key": "string-flag",
                  "name": "string-flag",
                  "target": { "value": { "value": { "string": "hello" } } },
                  "rules": []
                },
                {
                  "version": "1",
                  "type": "number",
                  "key": "number-flag",
                  "name": "number-flag",
                  "target": { "value": { "value": { "number": 42 } } },
                  "rules": []
                },
                {
                  "version": "1",
                  "type": "json",
                  "key": "json-flag",
                  "name": "json-flag",
                  "target": { "value": { "value": { "json": { "nested": true, "count": 3.5 } } } },
                  "rules": []
                }
              ]
            }
        """.trimIndent()
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

    /**
     * Parses a raw rules JSON string the same way [com.zenmanage.kotlin.api.ApiClient.getRules] does,
     * so tests exercise real Jackson deserialization (including [FlagType.fromWireValue]) rather than
     * constructing domain objects directly.
     */
    private class JsonRulesApiClient(private val rawJson: String) : ApiClient(
        environmentToken = "mob_test",
        apiEndpoint = "https://example.com",
        logger = NullLogger(),
        usageReportingEnabled = false,
        sdkVersion = "test",
        clientAgent = "zenmanage-kotlin-test"
    ) {
        private val objectMapper: ObjectMapper =
            jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        override fun getRules(): RulesResponse = objectMapper.readValue(rawJson, RulesResponse::class.java)

        override fun reportUsage(key: String, context: Context?, defaultValue: Any?): Unit = Unit
    }

    private class RecordingLogger : Logger {
        val warnings: MutableList<String> = mutableListOf()

        override fun debug(message: String): Unit = Unit

        override fun info(message: String): Unit = Unit

        override fun warn(message: String): Unit {
            warnings.add(message)
        }

        override fun error(message: String, throwable: Throwable): Unit = Unit
    }
}
