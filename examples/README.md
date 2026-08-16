# Zenmanage Kotlin SDK Examples

This folder contains self-contained examples mirroring the Java and PHP SDK example set.

## Examples

- `simple_flags.kt`: Basic flag retrieval and typed access
- `context_based_flags.kt`: Context-aware targeting with attributes
- `defaults.kt`: Inline and collection defaults
- `caching.kt`: Memory, filesystem, null, and SharedPreferences cache options
- `ab_testing.kt`: Experiment variants and segmentation
- `percentage_rollouts.kt`: Deterministic rollout behavior
- `android-app/`: Standalone Android app module showing SharedPreferences cache integration

## Run Locally

1. Export your environment token:

```bash
export ZENMANAGE_ENVIRONMENT_TOKEN="mob_your_mobile_key_here"
```

2. Run tests and coverage:

```bash
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification
```

3. Copy example snippets into your app and adapt flag keys to your dashboard setup.

## Android Example App

The Android sample lives in examples/android-app and can be opened directly in Android Studio.
It uses includeBuild("../..") to consume this SDK source locally without publishing.
