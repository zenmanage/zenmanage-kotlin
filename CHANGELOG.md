# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

## [1.0.2] - 2026-10-30

### Changed
- Upgraded Jackson from 2.18.0 to 2.22.4 (`jackson-databind` and `jackson-module-kotlin`). Both are `api` dependencies, so apps pick up the new version transitively.
- Upgraded the Kotlin Gradle plugin from 2.2.21 to 2.4.21. `apiVersion` and `languageVersion` stay at 2.0, so Kotlin 2.0+ consumers can still read the published metadata.

## [1.0.1] - 2026-10-28

- Publishing: the release workflow uploads to Maven Central on each GitHub Release. The in-repo publishing checklist was removed.

## [1.0.0] - 2026-10-25

First stable release. From 1.0.0 on, the public API follows semantic versioning.

- Added `ZenmanageSDK.VERSION`, the single source for the SDK version. It is sent to the API as `X-ZEN-CLIENT-AGENT: zenmanage-kotlin/<version>` on every request, including the CDN rules fetch. Previously the default was hard-coded to `0.1.0`.
- Rules are now evaluated lowest `position` first, regardless of the order they arrive in the payload, matching the Swift, PHP, Ruby, and Flutter SDKs. Rules without a `position` count as `0`, and rules that share a position keep their payload order.
- An out-of-range rollout percentage now throws `EvaluationException` (a `ZenmanageException`) instead of `IllegalArgumentException`, so callers can catch every SDK error with one type.
- The artifact is compiled to Java 11 bytecode. Earlier builds targeted JDK 21 and would not load on JDK 11 through 20 runtimes or on Android.
- Publishing moved from the retired Sonatype OSSRH to the Maven Central Portal.
- Fixed `Flag.asBool()` returning `false` instead of `true` for non-boolean-typed flags in some cases — most notably `json`-typed flags (which had no fallback branch at all), but also `string`/`number`-typed flags whose value didn't happen to parse as a boolean literal or a nonzero number. Per the documented cross-type coercion contract, `asBool()` now always returns `true` for any non-boolean-typed flag, regardless of the underlying value.
- Fixed `FlagManager.single()` throwing when the rules fetch itself failed (e.g. an unreachable/invalid environment key), even when the caller supplied an inline default or a `DefaultsCollection` entry. `single()` now falls back to serving that default instead, matching the reference PHP SDK's `loadFlagsOrFallBackToDefaults()` pattern.

## [0.2.0] - 2026-09-25

- Added `Flag.asJson(): Any` — the SDK now supports the `json` flag type, exposing structured (object/array) flag values alongside the existing `asBool()`/`asString()`/`asNumber()` accessors. A JSON object decodes to `Map<String, Any?>`, a JSON array decodes to `List<Any?>`.
- `Map`/`List` default values passed to `single(key, default)` or `DefaultsCollection` are now typed as `json` (previously coerced to a stringified fallback).
- See the README's "Value Types & Cross-Type Coercion" section for the full coercion reference.

## [0.1.0] - 2026-05-08

- Initial Kotlin SDK release.
- Added deterministic percentage rollouts.
- Added memory, filesystem, null, and Android SharedPreferences caches.
- Added examples and publishing guidance.
