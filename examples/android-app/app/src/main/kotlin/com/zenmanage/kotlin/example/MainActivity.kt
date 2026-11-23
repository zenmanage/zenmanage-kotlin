package com.zenmanage.kotlin.example

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.zenmanage.kotlin.Zenmanage
import com.zenmanage.kotlin.cache.SharedPreferencesCache
import com.zenmanage.kotlin.config.ConfigBuilder
import com.zenmanage.kotlin.context.Attribute
import com.zenmanage.kotlin.context.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// A plain Activity with framework widgets only: AndroidX AppCompat, Material and core-ktx now require
// API 23+, and this example is built at the SDK's promised minimum (API 21) in CI.
class MainActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var zenmanage: Zenmanage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        zenmanage = createZenmanage()

        val statusText: TextView = findViewById(R.id.statusText)
        val evaluateButton: Button = findViewById(R.id.evaluateButton)

        evaluateButton.setOnClickListener {
            statusText.text = getString(R.string.loading_message)

            scope.launch {
                val flagEnabled = withContext(Dispatchers.IO) {
                    runCatching {
                        val context = Context.single("user", "android-demo-user", "Android Demo User")
                            .addAttribute(Attribute("platform", listOf("android")))
                            .addAttribute(Attribute("app_version", listOf(BuildConfig.VERSION_NAME)))

                        zenmanage.flags()
                            .withContext(context)
                            .single("new-mobile-experience", false)
                            .isEnabled()
                    }.getOrElse { false }
                }

                statusText.text = if (flagEnabled) {
                    getString(R.string.flag_enabled)
                } else {
                    getString(R.string.flag_disabled)
                }
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun createZenmanage(): Zenmanage {
        val sharedPrefs = getSharedPreferences("zenmanage", MODE_PRIVATE)

        val config = ConfigBuilder.create()
            .withEnvironmentToken(BuildConfig.ZENMANAGE_TOKEN)
            .withCache(SharedPreferencesCache(sharedPrefs))
            .withCacheTtlSeconds(3600)
            .build()

        return Zenmanage(config)
    }
}
