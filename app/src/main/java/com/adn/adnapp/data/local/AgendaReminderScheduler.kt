package com.adn.adnapp.data.local

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.adn.adnapp.domain.model.AgendaItem
import java.time.LocalDateTime
import java.time.ZoneId

class AgendaReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(item: AgendaItem) {
        if (item.kind != com.adn.adnapp.domain.model.AgendaItemKind.REMINDER || item.date == null || item.time.isBlank()) return
        val at = runCatching {
            LocalDateTime.parse("${item.date}T${item.time}")
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.getOrNull() ?: return
        if (at <= System.currentTimeMillis()) return
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pendingIntent(item))
    }

    fun cancel(itemId: String) {
        alarmManager.cancel(pendingIntent(itemId))
    }

    private fun pendingIntent(item: AgendaItem): PendingIntent = pendingIntent(item.id, item.title)

    private fun pendingIntent(itemId: String, title: String = "") = PendingIntent.getBroadcast(
        context,
        itemId.hashCode(),
        Intent(context, AgendaReminderReceiver::class.java)
            .putExtra(AgendaReminderReceiver.EXTRA_ID, itemId)
            .putExtra(AgendaReminderReceiver.EXTRA_TITLE, title),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
