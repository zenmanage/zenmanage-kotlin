plugins {
    id("com.android.application")
}

val zenmanageToken: String =
    (project.findProperty("zenmanageToken") as String?)
        ?: System.getenv("ZENMANAGE_ENVIRONMENT_TOKEN")
        ?: "mob_your_mobile_key_here"

android {
    namespace = "com.zenmanage.kotlin.example"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.zenmanage.kotlin.example"
        // Keep this at the lowest API level the README promises, so CI catches a dependency that needs more.
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "ZENMANAGE_TOKEN", "\"$zenmanageToken\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // Dispatchers.Main for the activity's coroutine scope.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    implementation("com.zenmanage:zenmanage-kotlin:1.1.0")
}
