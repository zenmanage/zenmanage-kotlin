import com.zenmanage.kotlin.Zenmanage
import com.zenmanage.kotlin.config.ConfigBuilder
import com.zenmanage.kotlin.flags.DefaultsCollection

fun main() {
    val zenmanage = Zenmanage(
        ConfigBuilder.create()
            .withEnvironmentToken(System.getenv("ZENMANAGE_ENVIRONMENT_TOKEN") ?: "mob_your_mobile_key_here")
            .build()
    )

    println("Inline default: ${zenmanage.flags().single("missing-flag", true).isEnabled()}")

    val defaults = DefaultsCollection.fromMap(
        mapOf(
            "feature-a" to true,
            "api-version" to "v2",
            "max-items" to 100
        )
    )

    val version = zenmanage.flags().withDefaults(defaults).single("api-version").asString()
    println("Collection default: $version")

    val inlineWins = zenmanage.flags().withDefaults(defaults).single("api-version", "v3").asString()
    println("Inline default precedence: $inlineWins")
}
