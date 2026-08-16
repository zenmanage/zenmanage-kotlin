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
- Kotlin 1.9+
- Android apps: API level 21+ (for SharedPreferences integration)

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.zenmanage:zenmanage-kotlin:0.1.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'com.zenmanage:zenmanage-kotlin:0.1.0'
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

### Percentage Rollouts

```kotlin
import com.zenmanage.kotlin.context.Context

val context = Context.single("user", "user-42")

val inRollout = zenmanage.flags()
    .withContext(context)
    .single("new-checkout-flow", false)
    .isEnabled()
```

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

## Publishing

See [docs/PUBLISHING_NEXT_STEPS.md](docs/PUBLISHING_NEXT_STEPS.md) for a checklist covering Maven Central, GitHub Packages, and JitPack/jCenter compatibility options.
