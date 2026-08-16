package com.zenmanage.kotlin.example

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zenmanage.kotlin.Zenmanage
import com.zenmanage.kotlin.cache.SharedPreferencesCache
import com.zenmanage.kotlin.config.ConfigBuilder
import com.zenmanage.kotlin.context.Attribute
import com.zenmanage.kotlin.context.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var zenmanage: Zenmanage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        zenmanage = createZenmanage()

        val statusText: TextView = findViewById(R.id.statusText)
        val evaluateButton: Button = findViewById(R.id.evaluateButton)

        evaluateButton.setOnClickListener {
            statusText.text = getString(R.string.loading_message)

            lifecycleScope.launch {
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
