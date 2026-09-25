# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

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
