package com.adn.adnapp.data.local

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.adn.adnapp.R

class AgendaReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Agenda", NotificationManager.IMPORTANCE_DEFAULT))
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "Recordatorio" }
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.adn_logo)
            .setContentTitle("Agenda")
            .setContentText(title)
            .setAutoCancel(true)
            .build()
        manager.notify(intent.getStringExtra(EXTRA_ID).orEmpty().hashCode(), notification)
    }

    companion object {
        const val EXTRA_ID = "agendaItemId"
        const val EXTRA_TITLE = "agendaTitle"
        private const val CHANNEL = "agenda-reminders"
    }
}
