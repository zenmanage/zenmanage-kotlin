// AGP 9 has built-in Kotlin, but it defaults to an older compiler than the one
// the SDK is built with (2.4.x). Putting the Kotlin Gradle plugin on the build
// classpath makes AGP use this version, so the app can read the SDK's metadata.
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    id("com.android.application") version "9.5.2" apply false
}
