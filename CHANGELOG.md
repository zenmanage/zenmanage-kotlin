# Changelog

All notable changes to this project will be documented in this file.

## [0.2.0] - 2026-09-25

- Added `Flag.asJson(): Any` — the SDK now supports the `json` flag type, exposing structured (object/array) flag values alongside the existing `asBool()`/`asString()`/`asNumber()` accessors. A JSON object decodes to `Map<String, Any?>`, a JSON array decodes to `List<Any?>`.
- `Map`/`List` default values passed to `single(key, default)` or `DefaultsCollection` are now typed as `json` (previously coerced to a stringified fallback).
- See the README's "Value Types & Cross-Type Coercion" section for the full coercion reference.

## [0.1.0] - 2026-05-08

- Initial Kotlin SDK release.
- Added deterministic percentage rollouts.
- Added memory, filesystem, null, and Android SharedPreferences caches.
- Added examples and publishing guidance.
