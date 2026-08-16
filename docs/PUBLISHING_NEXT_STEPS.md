# Publishing Next Steps for zenmanage-kotlin

This guide outlines practical next steps to publish the Kotlin SDK to major public package repositories.

## 1. Repository Readiness Checklist

Before publishing anywhere:

1. Ensure legal/project metadata is complete in Gradle and POM fields:
   - group/artifact/version
   - license
   - SCM URLs
   - developer/contact information
2. Ensure all CI checks are green:
   - `./gradlew clean check`
   - coverage gates
3. Tag a release candidate in Git:
   - `git tag v0.1.0`
   - `git push origin v0.1.0`
4. Prepare release notes/changelog.

## 2. Publish to Maven Central (Sonatype)

Maven Central should be the primary channel for Kotlin/Android dependency resolution.

### One-time setup

1. Create a Sonatype Central account: https://central.sonatype.com/
2. Verify namespace ownership for the group (for example, `com.zenmanage`).
3. Generate and securely store a GPG keypair for artifact signing.
4. Configure credentials as CI or local environment variables:
   - `SONATYPE_USERNAME`
   - `SONATYPE_PASSWORD`
   - `GPG_PRIVATE_KEY`
   - `GPG_PASSPHRASE`

### Release command

```bash
./gradlew clean publish sign
```

### Validation

1. Confirm package appears on Maven Central with sources/javadocs.
2. Validate install in a clean sample app.

## 3. Publish to GitHub Packages

Use GitHub Packages as a secondary distribution channel.

### Setup

1. Ensure repository package permissions are enabled.
2. Configure credentials:
   - `GITHUB_ACTOR`
   - `GITHUB_TOKEN` (with `write:packages`)

### Publish command

```bash
./gradlew publishAllPublicationsToGitHubPackagesRepository
```

## 4. JitPack and jCenter Compatibility Path

JitPack can provide immediate tag-based distribution and a migration path for legacy jCenter consumers.

### JitPack steps

1. Ensure repository is public and build is reproducible with `./gradlew clean test`.
2. Create and push a version tag:

```bash
git tag v0.1.0
git push origin v0.1.0
```

3. Verify build at `https://jitpack.io/#zenmanage/zenmanage-kotlin/v0.1.0`.

### jCenter note

- jCenter is read-only and no longer accepts new package onboarding.
- For teams still resolving from jCenter mirrors, publish to Maven Central and optionally JitPack, then provide migration guidance in README/UPGRADING docs.

## 5. CI/CD Automation Recommendations

Automate release quality and deployment with GitHub Actions:

1. `ci.yml`: build/test/coverage on every PR.
2. `release.yml`: publish on semver tag pushes.
3. Keep signing and repository credentials in GitHub Actions secrets.

## 6. Post-Publish Validation

After each release:

1. Validate artifact metadata, checksums, and signatures.
2. Confirm Android and JVM sample apps resolve and run.
3. Announce release with upgrade notes.

## 7. Versioning Policy

Recommended approach:

1. Follow SemVer strictly.
2. Maintain `CHANGELOG.md` in Keep a Changelog style.
3. Provide `UPGRADING.md` for breaking changes.
