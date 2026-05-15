package com.lecturevault.app.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val id = intent.getIntExtra("id", 0)
        val notif = NotificationCompat.Builder(context, "reminders")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("LectureVault")
            .setContentText(title)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(id, notif)
    }
}

object ReminderScheduler {
    fun schedule(context: Context, id: Int, title: String, triggerAtMillis: Long) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title); putExtra("id", id)
        }
        val pi = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(AlarmManager::class.java)
        am.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
    }
}
