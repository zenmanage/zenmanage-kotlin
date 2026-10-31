package com.zenmanage.kotlin

/**
 * Identifies this release of the SDK.
 *
 * [VERSION] is sent to the API as part of the `X-ZEN-CLIENT-AGENT` header
 * (`zenmanage-kotlin/<version>`). Keep it in step with `version` in `gradle.properties` and with
 * the released section of `CHANGELOG.md` when you cut a release; `ZenmanageSDKVersionTest`
 * fails if they drift.
 */
public object ZenmanageSDK {
    public const val VERSION: String = "1.0.2"
}
