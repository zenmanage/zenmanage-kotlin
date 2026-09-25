import com.zenmanage.kotlin.Zenmanage
import com.zenmanage.kotlin.config.ConfigBuilder

fun main() {
    val config = ConfigBuilder.create()
        .withEnvironmentToken(System.getenv("ZENMANAGE_ENVIRONMENT_TOKEN") ?: "mob_your_mobile_key_here")
        .build()

    val zenmanage = Zenmanage(config)

    val boolFlag = zenmanage.flags().single("example-boolean-flag", false)
    println("Boolean flag: ${boolFlag.isEnabled()}")

    val stringFlag = zenmanage.flags().single("example-string-flag", "default-value")
    println("String flag: ${stringFlag.asString()}")

    val numberFlag = zenmanage.flags().single("example-number-flag", 42)
    println("Number flag: ${numberFlag.asNumber()}")

    val jsonFlag = zenmanage.flags().single("example-json-flag", mapOf("theme" to "dark", "maxItems" to 10))
    println("JSON flag: ${jsonFlag.asJson()}")

    val all = zenmanage.flags().all()
    println("Total flags loaded: ${all.size}")
}
