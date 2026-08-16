import com.zenmanage.kotlin.Zenmanage
import com.zenmanage.kotlin.cache.FileSystemCache
import com.zenmanage.kotlin.cache.NullCache
import com.zenmanage.kotlin.config.ConfigBuilder
import java.nio.file.Paths

fun main() {
    val token = System.getenv("ZENMANAGE_ENVIRONMENT_TOKEN") ?: "mob_your_mobile_key_here"

    val memory = Zenmanage(
        ConfigBuilder.create()
            .withEnvironmentToken(token)
            .withCacheBackend("memory")
            .withCacheTtlSeconds(3600)
            .build()
    )

    println("Memory cache flag: ${memory.flags().single("example-flag", true).isEnabled()}")

    val filesystem = Zenmanage(
        ConfigBuilder.create()
            .withEnvironmentToken(token)
            .withCache(FileSystemCache(Paths.get(System.getProperty("java.io.tmpdir"), "zenmanage-cache")))
            .withCacheTtlSeconds(7200)
            .build()
    )

    println("Filesystem cache flag: ${filesystem.flags().single("example-flag", true).isEnabled()}")

    val noCache = Zenmanage(
        ConfigBuilder.create()
            .withEnvironmentToken(token)
            .withCache(NullCache())
            .build()
    )

    println("Null cache flag: ${noCache.flags().single("example-flag", true).isEnabled()}")
}
