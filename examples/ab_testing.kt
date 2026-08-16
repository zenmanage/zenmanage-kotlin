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

    val users = listOf("user-001", "user-002", "user-003")
    users.forEach { userId ->
        val context = Context.single("user", userId)
            .addAttribute(Attribute("country").addValue("US"))

        val variant = zenmanage.flags()
            .withContext(context)
            .single("checkout-flow", "control")
            .asString()

        println("$userId -> $variant")
    }
}
