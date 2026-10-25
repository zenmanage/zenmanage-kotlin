import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.21"
    `java-library`
    id("com.vanniktech.maven.publish") version "0.37.0"
    jacoco
}

group = property("group") as String
version = property("version") as String

repositories {
    mavenCentral()
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.0")

    compileOnly("com.google.android:android:4.1.1.4")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.1")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("com.google.android:android:4.1.1.4")
}

kotlin {
    // Build with JDK 21, but emit Java 11 bytecode so the artifact runs on the JDK 11+ and
    // Android consumers the README promises. Without this, the class files target JDK 21.
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        // Emit Kotlin 2.0 metadata so consumers on Kotlin 2.0 and newer can read the artifact.
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
        languageVersion.set(KotlinVersion.KOTLIN_2_0)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.freeCompilerArgs.add("-Xjsr305=strict")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "com/zenmanage/kotlin/errors/**",
                        "com/zenmanage/kotlin/api/**",
                        "com/zenmanage/kotlin/flags/FlagData*",
                        "com/zenmanage/kotlin/flags/RulesResponse*",
                        "com/zenmanage/kotlin/flags/Rule*",
                        "com/zenmanage/kotlin/flags/RawFlagValue*",
                        "com/zenmanage/kotlin/flags/TargetValue*",
                        "com/zenmanage/kotlin/flags/FlagTarget*",
                        "com/zenmanage/kotlin/flags/RolloutData*",
                        "com/zenmanage/kotlin/flags/FlagType*"
                    )
                }
            }
        )
    )
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    classDirectories.setFrom(tasks.jacocoTestReport.get().classDirectories)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.65".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.34".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

// Maven Central (Central Portal). The plugin builds the sources and Javadoc jars, and reads the POM
// name, license, developer and SCM fields from the POM_* keys in gradle.properties. It signs the
// publication when a signing key is configured. Credentials and key material come from Gradle
// properties or ORG_GRADLE_PROJECT_* environment variables; see docs/PUBLISHING_NEXT_STEPS.md.
mavenPublishing {
    publishToMavenCentral()

    if (hasProperty("signingInMemoryKey") || hasProperty("signing.keyId")) {
        signAllPublications()
    }

    coordinates(group.toString(), "zenmanage-kotlin", version.toString())
}

publishing {
    // Secondary channel: GitHub Packages. Consumers need a token with read:packages to resolve it.
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/zenmanage/zenmanage-kotlin")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
