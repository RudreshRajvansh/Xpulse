package com.rudresh.xpulse.core.data.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerConfig @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("xpulse_server", Context.MODE_PRIVATE)

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) {
            val cleaned = value.trim().trimEnd('/')
            val withScheme = if (cleaned.startsWith("http")) cleaned else "http://$cleaned"
            prefs.edit().putString(KEY_BASE_URL, withScheme).apply()
        }

    fun reset() {
        prefs.edit().remove(KEY_BASE_URL).apply()
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://xpulse-backend.onrender.com"
        private const val KEY_BASE_URL = "base_url"
    }
}
