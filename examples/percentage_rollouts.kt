import com.zenmanage.kotlin.Zenmanage
import com.zenmanage.kotlin.config.ConfigBuilder
import com.zenmanage.kotlin.context.Context

fun main() {
    val zenmanage = Zenmanage(
        ConfigBuilder.create()
            .withEnvironmentToken(System.getenv("ZENMANAGE_ENVIRONMENT_TOKEN") ?: "mob_your_mobile_key_here")
            .build()
    )

    val users = listOf("user-1", "user-2", "user-3", "user-4", "user-5")
    users.forEach { userId ->
        val enabled = zenmanage.flags()
            .withContext(Context.single("user", userId))
            .single("new-checkout-flow", false)
            .isEnabled()

        println("$userId in rollout? $enabled")
    }

    val stable = Context.single("user", "stable-user")
    val first = zenmanage.flags().withContext(stable).single("rollout-flag", false).isEnabled()
    val second = zenmanage.flags().withContext(stable).single("rollout-flag", false).isEnabled()
    println("Deterministic result: ${first == second}")
}
