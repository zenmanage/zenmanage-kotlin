package com.zenmanage.kotlin.flags

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zenmanage.kotlin.api.ApiClient
import com.zenmanage.kotlin.cache.Cache
import com.zenmanage.kotlin.config.Logger
import com.zenmanage.kotlin.context.Context
import com.zenmanage.kotlin.errors.EvaluationException
import com.zenmanage.kotlin.rollout.RolloutBucketer
import com.zenmanage.kotlin.rules.RuleEngine

public class FlagManager(
    private val apiClient: ApiClient,
    private val cache: Cache,
    private val ruleEngine: RuleEngine,
    private val cacheTtlSeconds: Int,
    private val logger: Logger,
    private val sharedState: SharedState = SharedState(),
    private val context: Context = Context("anonymous"),
    private val defaults: DefaultsCollection = DefaultsCollection(),
    private val objectMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
) {
    public fun all(): List<Flag> {
        ensureRulesLoaded()
        return sharedState.flags.orEmpty().filter { it.type != FlagType.UNKNOWN }.map(::evaluateFlag)
    }

    public fun single(key: String): Flag = single(key, null)

    public fun single(key: String, defaultValue: Any?): Flag {
        ensureRulesLoaded()

        val matched = sharedState.flags.orEmpty().firstOrNull { it.key == key && it.type != FlagType.UNKNOWN }
        if (matched != null) {
            reportUsage(key, usageContext(), resolveEffectiveDefault(key, defaultValue))
            return evaluateFlag(matched)
        }

        if (defaultValue != null) {
            reportUsage(key, usageContext(), defaultValue)
            return createFlagFromDefault(key, defaultValue)
        }

        if (defaults.has(key)) {
            val fallbackDefault = defaults.get(key)
            reportUsage(key, usageContext(), fallbackDefault)
            return createFlagFromDefault(key, fallbackDefault)
        }

        throw EvaluationException("Flag not found: $key")
    }

    public fun withContext(newContext: Context): FlagManager {
        return FlagManager(apiClient, cache, ruleEngine, cacheTtlSeconds, logger, sharedState, newContext, defaults, objectMapper)
    }

    public fun withDefaults(newDefaults: DefaultsCollection): FlagManager {
        return FlagManager(apiClient, cache, ruleEngine, cacheTtlSeconds, logger, sharedState, context, newDefaults, objectMapper)
    }

    public fun reportUsage(key: String, usageContext: Context?, defaultValue: Any? = null): Unit {
        apiClient.reportUsage(key, usageContext, defaultValue)
    }

    public fun refreshRules(): Unit {
        logger.info("Refreshing rules from API")
        loadRulesFromApi()
    }

    private fun resolveEffectiveDefault(key: String, defaultValue: Any?): Any? {
        if (defaultValue != null) {
            return defaultValue
        }
        return if (defaults.has(key)) defaults.get(key) else null
    }

    private fun usageContext(): Context? {
        if (context.type == "anonymous" && context.name == null && context.identifier == null && context.getAttributes().isEmpty()) {
            return null
        }
        return context
    }

    @Synchronized
    private fun ensureRulesLoaded(): Unit {
        if (sharedState.flags != null) {
            return
        }

        cache.get(CACHE_KEY)?.let { cached ->
            runCatching {
                objectMapper.readValue(cached, RulesResponse::class.java)
            }.onSuccess { cachedResponse ->
                val cachedFlags = cachedResponse.flags
                if (cachedFlags != null) {
                    val flags = cachedFlags.map(Flag::fromData)
                    logUnknownFlagTypes(flags)
                    sharedState.flags = flags
                    return
                }
            }.onFailure {
                logger.warn("Failed to parse cached rules")
            }
        }

        loadRulesFromApi()
    }

    private fun loadRulesFromApi(): Unit {
        val response = apiClient.getRules()
        val flags = response.flags.orEmpty().map(Flag::fromData)
        logUnknownFlagTypes(flags)
        sharedState.flags = flags

        runCatching {
            cache.set(CACHE_KEY, objectMapper.writeValueAsString(response), cacheTtlSeconds.toLong())
        }.onFailure {
            logger.warn("Failed to serialize rules for cache")
        }
    }

    private fun logUnknownFlagTypes(flags: List<Flag>): Unit {
        flags.filter { it.type == FlagType.UNKNOWN }.forEach { flag ->
            logger.warn("Flag '${flag.key}' has an unrecognized type; falling back to the caller's default value")
        }
    }

    private fun evaluateFlag(flag: Flag): Flag {
        var target = flag.target
        var rules = flag.rules

        val rollout = flag.rollout
        if (rollout != null) {
            val inBucket = RolloutBucketer.isInBucket(rollout.salt, context.identifier, rollout.percentage)
            if (inBucket) {
                target = rollout.target
                rules = rollout.rules ?: emptyList()
            }
        }

        if (rules.isEmpty()) {
            return flag.copy(target = target, rules = rules)
        }

        val matchedRule = ruleEngine.evaluate(rules, context)
        if (matchedRule?.value?.value != null) {
            val remappedTarget = mapRuleValueOntoTarget(target, matchedRule.value!!)
            return flag.copy(target = remappedTarget, rules = rules)
        }

        return flag.copy(target = target, rules = rules)
    }

    private fun mapRuleValueOntoTarget(target: FlagTarget?, ruleValue: RuleValue): FlagTarget {
        return FlagTarget(
            version = target?.version,
            expiredAt = target?.expiredAt,
            publishedAt = target?.publishedAt,
            scheduledAt = target?.scheduledAt,
            value = TargetValue(
                version = ruleValue.version,
                value = ruleValue.value
            )
        )
    }

    private fun createFlagFromDefault(key: String, defaultValue: Any?): Flag {
        val type = when (defaultValue) {
            is Boolean -> FlagType.BOOLEAN
            is Number -> FlagType.NUMBER
            else -> FlagType.STRING
        }

        val target = FlagTarget(
            value = TargetValue(
                value = RawFlagValue.fromKotlinValue(defaultValue)
            )
        )

        return Flag(
            version = "1",
            type = type,
            key = key,
            name = key,
            target = target,
            rules = emptyList(),
            rollout = null
        )
    }

    public class SharedState {
        var flags: List<Flag>? = null
    }

    private companion object {
        private const val CACHE_KEY: String = "zenmanage_rules"
    }
}
