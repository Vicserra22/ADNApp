package com.adn.adnapp.data.local

import android.content.Context
import android.content.Intent

class HealthConnectLauncher(private val context: Context) {
    fun openSettings(): Boolean = runCatching {
        context.startActivity(Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    }.getOrDefault(false)
}
