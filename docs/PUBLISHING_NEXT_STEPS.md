# Publishing zenmanage-kotlin

The SDK ships to Maven Central through the Sonatype Central Portal (OSSRH was retired in 2025,
so the old `s01.oss.sonatype.org` flow no longer works). GitHub Packages is a secondary channel.

Coordinates: `com.zenmanage:zenmanage-kotlin`

## One-time setup

1. **Central Portal account and namespace.** Sign in at https://central.sonatype.com and claim the
   `com.zenmanage` namespace. Verify it with the DNS TXT record the portal gives you on zenmanage.com.
2. **User token.** In the portal, generate a user token. Its username and password are what Gradle
   uses. They are not your login.
3. **GPG key.** Create a signing key (`gpg --full-generate-key`), then publish the public key so the
   portal can verify signatures:
   `gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>`
4. **Credentials for Gradle.** Set these as environment variables (CI) or in `~/.gradle/gradle.properties`:
   - `ORG_GRADLE_PROJECT_mavenCentralUsername`: token username
   - `ORG_GRADLE_PROJECT_mavenCentralPassword`: token password
   - `ORG_GRADLE_PROJECT_signingInMemoryKey`: output of `gpg --export-secret-keys --armor <KEY_ID>`
   - `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword`: the key's passphrase, if it has one

Without the signing properties, `publishToMavenCentral` still uploads, but the portal rejects unsigned
artifacts.

## Release steps

1. Make sure the version matches in three places: `version` in `gradle.properties`,
   `ZenmanageSDK.VERSION` in `src/main/kotlin/com/zenmanage/kotlin/ZenmanageSDK.kt`, and the dated
   `## [x.y.z] - YYYY-MM-DD` section in `CHANGELOG.md`. `ZenmanageSDKVersionTest` checks all three.
2. Run the full gate: `./gradlew clean check` (unit tests and the coverage gate).
3. Upload: `./gradlew publishToMavenCentral`. This creates a deployment in the Central Portal.
   Review it there and click **Publish**. (Use `./gradlew publishAndReleaseToMavenCentral` only if you
   want the portal to release it automatically.)
4. Confirm the artifact appears at https://repo1.maven.org/maven2/com/zenmanage/zenmanage-kotlin/ with
   `.jar`, `-sources.jar`, `-javadoc.jar`, `.pom` and `.asc` files.
5. Tag the release as plain `x.y.z` (no `v` prefix): `git tag 1.0.0 && git push origin 1.0.0`, then
   create the GitHub Release from the tag.
6. Optional, GitHub Packages: `./gradlew publishAllPublicationsToGitHubPackagesRepository`, with
   `GITHUB_ACTOR` and a `GITHUB_TOKEN` that has `write:packages`.
7. Register the SDK with the website: add `Kotlin` to `config('zenmanage.published_sdks')` in the
   api repo. The client agent family `zenmanage-kotlin` is already allow-listed in
   `EnsureClientAgentIsValid`.

## Checking a build without publishing

- `./gradlew publishToMavenLocal` installs to `~/.m2`, so you can inspect the POM and jars.
- The POM's name, license, developer and SCM fields come from the `POM_*` keys in `gradle.properties`.
  Edit them there, not in `build.gradle.kts`.
