# Zenmanage Kotlin SDK

Add feature flags to your Kotlin and Android applications in minutes. Control feature rollouts, run A/B tests, and manage runtime configuration without redeploying.

## Why Zenmanage?

- Fast evaluations with local caching
- Context-aware targeting for users and organizations
- Deterministic percentage rollouts (CRC32 bucketing)
- Graceful defaults and fail-safe behavior
- Built for Kotlin-first and Java interop use cases

## Requirements

- JDK 11+
- Kotlin 2.0+
- Android apps: API level 21+ (for SharedPreferences integration)

## Installation

The SDK is published to [Maven Central](https://central.sonatype.com/artifact/com.zenmanage/zenmanage-kotlin).

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.zenmanage:zenmanage-kotlin:1.0.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'com.zenmanage:zenmanage-kotlin:1.0.0'
}
```

## Get Started

```kotlin
import com.zenmanage.kotlin.Zenmanage
import com.zenmanage.kotlin.config.ConfigBuilder

val config = ConfigBuilder.create()
    .withEnvironmentToken("mob_your_mobile_key_here")
    .build()

val zenmanage = Zenmanage(config)

val enabled = zenmanage.flags()
    .single("new-dashboard", false)
    .isEnabled()
```

## Key Compatibility

- Supported token prefixes: `srv_` and `mob_`
- Rejected token prefix: `cli_`

## Common Use Cases

### Context-Based Evaluation

```kotlin
import com.zenmanage.kotlin.context.Attribute
import com.zenmanage.kotlin.context.Context

val context = Context.single("user", "user-123", "Alice")
    .addAttribute(Attribute("country", listOf("US")))

val premiumEnabled = zenmanage.flags()
    .withContext(context)
    .single("premium-feature", false)
    .isEnabled()
```

### Defaults Collection

```kotlin
import com.zenmanage.kotlin.flags.DefaultsCollection

val defaults = DefaultsCollection.fromMap(
    mapOf(
        "new-ui" to true,
        "api-version" to "v2",
        "max-items" to 100
    )
)

val version = zenmanage.flags()
    .withDefaults(defaults)
    .single("api-version")
    .asString()
```

### JSON Flags

```kotlin
val config = zenmanage.flags()
    .single("checkout-config", mapOf("maxItems" to 10, "theme" to "dark"))
    .asJson()
```

`asJson()` decodes both JSON objects (as `Map<String, Any?>`) and JSON arrays (as `List<Any?>`).

### Percentage Rollouts

```kotlin
import com.zenmanage.kotlin.context.Context

val context = Context.single("user", "user-42")

val inRollout = zenmanage.flags()
    .withContext(context)
    .single("new-checkout-flow", false)
    .isEnabled()
```

## Value Types & Cross-Type Coercion

A flag's `type` is one of `boolean`, `string`, `number`, or `json`. Each type has a matching accessor (`asBool()`, `asString()`, `asNumber()`, `asJson()`), plus `isEnabled()` for boolean flags specifically.

`asJson()` returns the decoded value: a JSON object decodes to `Map<String, Any?>`, a JSON array decodes to `List<Any?>`.

`asString()`, `asNumber()`, and `asJson()` each only recognize their own value wrapper (`{"string": …}`, `{"number": …}`, `{"json": …}`) and fall back to a safe zero value for every other type — they never attempt a lossy conversion between types:

| Called on →<br>Flag type ↓ | `asString()` | `asNumber()` | `asJson()` |
|---|---|---|---|
| `boolean` | `""` | `0` | `emptyMap()` |
| `string` | the string | `0` | `emptyMap()` |
| `number` | `""` | the number | `emptyMap()` |
| `json` | `""` | `0` | the decoded map/list |

`isEnabled()` returns `false` outright for any non-boolean flag — use it, not `asBool()`, when you specifically mean "is this boolean flag on". A `json` flag whose decoded value isn't itself an object or array (a bare JSON scalar such as `5` or `"x"`, which the API allows but which real flags won't normally use) makes `asJson()` return `emptyMap()` too — the same safe-zero-value behavior as calling it on a non-json flag, rather than wrapping the scalar or throwing.

`asBool()` predates this coercion convention and behaves differently: rather than switching on the flag's `type`, it coerces whatever the underlying value happens to be (a non-boolean `Number` is truthy if non-zero, a non-boolean `String` is parsed with `toBooleanStrictOrNull()`, and a `json` value — which `asBool()` does not look at — falls back to `false`). This SDK's `asBool()` is intentionally left as-is here; only `asJson()` follows the "recognize your own type, safe zero value otherwise" rule strictly.

**Default values** passed to `single(key, default)` or `DefaultsCollection` are typed from the Kotlin value itself: a `Map` or `List` default becomes a `json`-typed flag (not a stringified fallback), so `asJson()` on a missing flag with a `Map`/`List` default returns that value unchanged.

## Caching

### Memory cache (default)

```kotlin
ConfigBuilder.create()
    .withEnvironmentToken("mob_your_mobile_key_here")
    .withCacheBackend("memory")
    .build()
```

### Filesystem cache

```kotlin
ConfigBuilder.create()
    .withEnvironmentToken("srv_your_server_key_here")
    .withCacheBackend("filesystem")
    .withCacheDirectory("/tmp/zenmanage-cache")
    .build()
```

### Android SharedPreferences cache

```kotlin
import android.content.Context
import com.zenmanage.kotlin.cache.SharedPreferencesCache

fun createZenmanage(appContext: Context): Zenmanage {
    val prefs = appContext.getSharedPreferences("zenmanage", Context.MODE_PRIVATE)

    val config = ConfigBuilder.create()
        .withEnvironmentToken("mob_your_mobile_key_here")
        .withCache(SharedPreferencesCache(prefs))
        .build()

    return Zenmanage(config)
}
```

## Java Interop

The API is intentionally Java-friendly:

- Non-suspending, synchronous flag evaluation
- Builder-based configuration
- Nullable-safe default handling

## Examples

See [examples/README.md](examples/README.md) for the full sample set:

- simple_flags.kt
- context_based_flags.kt
- defaults.kt
- caching.kt
- ab_testing.kt
- percentage_rollouts.kt
- android-app/ (standalone Android integration sample)

## Quality and Coverage

Run tests and coverage report:

```bash
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification
```

Coverage report output:

- `build/reports/jacoco/test/html/index.html`
