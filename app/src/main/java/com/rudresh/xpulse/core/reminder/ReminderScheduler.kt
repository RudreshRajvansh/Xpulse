package com.rudresh.xpulse.core.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun ensureChannel() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Medicine reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Alerts when a dose is due"
        }
        manager.createNotificationChannel(channel)
    }

    fun schedule(requestCode: Int, medicineName: String, dose: String, atMillis: Long) {
        if (atMillis <= System.currentTimeMillis()) return
        alarmManager?.set(AlarmManager.RTC_WAKEUP, atMillis, pendingIntent(requestCode, medicineName, dose))
    }

    fun cancel(requestCode: Int) {
        alarmManager?.cancel(pendingIntent(requestCode, "", ""))
    }

    private fun pendingIntent(requestCode: Int, medicineName: String, dose: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_MEDICINE, medicineName)
            putExtra(ReminderReceiver.EXTRA_DOSE, dose)
            putExtra(ReminderReceiver.EXTRA_ID, requestCode)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    companion object {
        const val CHANNEL_ID = "xpulse_reminders"
    }
}
