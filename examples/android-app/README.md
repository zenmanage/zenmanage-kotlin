# Android Integration Example

This sample app shows how to integrate Zenmanage Kotlin SDK in an Android project with SharedPreferences caching.

## What it demonstrates

- SDK initialization with ConfigBuilder
- SharedPreferencesCache wiring
- Context-based flag evaluation
- Safe background execution for network-bound calls

## Prerequisites

- Android Studio Ladybug or newer
- Android SDK platform 35 installed
- JDK 17+

## Configure token

Provide a mobile or server token using one of these options:

1. Environment variable:

   export ZENMANAGE_ENVIRONMENT_TOKEN=mob_your_mobile_key_here

2. Gradle property:

   Add this to your local gradle.properties file:
   zenmanageToken=mob_your_mobile_key_here

## Run in Android Studio

1. Open the folder examples/android-app in Android Studio.
2. Sync Gradle.
3. Run app on an emulator or device.

## Run from command line

./gradlew :app:assembleDebug

## Notes

- The sample uses includeBuild("../..") so the app depends on your local SDK source without publishing.
- By default it evaluates the new-mobile-experience flag for a fixed demo user context.
