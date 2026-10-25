package com.zenmanage.kotlin

import com.zenmanage.kotlin.config.ConfigBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

/**
 * The version appears in three places: `ZenmanageSDK.VERSION` (sent to the API in the client agent
 * header), `version` in `gradle.properties` (the published artifact), and `CHANGELOG.md`. They are
 * updated by hand, so this test fails when they disagree.
 */
class ZenmanageSDKVersionTest {
    private fun repositoryFile(name: String): String = File(name).readText()

    @Test
    fun versionIsAThreePartSemanticVersion() {
        val parts = ZenmanageSDK.VERSION.split(".")

        assertThat(parts).hasSize(3)
        assertThat(parts).allSatisfy { assertThat(it.toIntOrNull()).isNotNull() }
    }

    @Test
    fun versionMatchesTheGradleArtifactVersion() {
        val properties = repositoryFile("gradle.properties")
        val gradleVersion = properties.lines()
            .firstOrNull { it.startsWith("version=") }
            ?.removePrefix("version=")

        assertThat(gradleVersion).isEqualTo(ZenmanageSDK.VERSION)
    }

    @Test
    fun changelogHasADatedSectionForTheVersion() {
        assertThat(repositoryFile("CHANGELOG.md")).contains("## [${ZenmanageSDK.VERSION}] - ")
    }

    @Test
    fun defaultConfigReportsTheSdkVersion() {
        val config = ConfigBuilder.create().withEnvironmentToken("srv_test").build()

        assertThat(config.sdkVersion).isEqualTo(ZenmanageSDK.VERSION)
        assertThat(config.clientAgent).isEqualTo("zenmanage-kotlin")
    }
}
