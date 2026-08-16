import com.zenmanage.kotlin.Zenmanage
import com.zenmanage.kotlin.config.ConfigBuilder
import com.zenmanage.kotlin.context.Attribute
import com.zenmanage.kotlin.context.Context

fun main() {
    val zenmanage = Zenmanage(
        ConfigBuilder.create()
            .withEnvironmentToken(System.getenv("ZENMANAGE_ENVIRONMENT_TOKEN") ?: "mob_your_mobile_key_here")
            .build()
    )

    val userContext = Context.single("user", "user-12345", "Jane Doe")
        .addAttribute(Attribute("country", listOf("US")))
        .addAttribute(Attribute("plan", listOf("premium")))

    val featureEnabled = zenmanage.flags()
        .withContext(userContext)
        .single("premium-feature", false)
        .isEnabled()

    println("Premium feature enabled: $featureEnabled")

    val fromMap = Context.fromMap(
        mapOf(
            "type" to "organization",
            "identifier" to "org-1",
            "name" to "Acme",
            "attributes" to listOf(
                mapOf("key" to "region", "values" to listOf(mapOf("value" to "us-east")))
            )
        )
    )

    val orgFeature = zenmanage.flags()
        .withContext(fromMap)
        .single("org-dashboard", false)
        .isEnabled()

    println("Org dashboard enabled: $orgFeature")
}
