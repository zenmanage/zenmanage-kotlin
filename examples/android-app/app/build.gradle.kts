plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val zenmanageToken: String =
    (project.findProperty("zenmanageToken") as String?)
        ?: System.getenv("ZENMANAGE_ENVIRONMENT_TOKEN")
        ?: "mob_your_mobile_key_here"

android {
    namespace = "com.zenmanage.kotlin.example"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zenmanage.kotlin.example"
        minSdk = 24
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    implementation("com.zenmanage:zenmanage-kotlin:1.0.1")
}
