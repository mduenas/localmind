package com.markduenas.localmind.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val TASK_SUMMARIES = "task_summaries"
    const val REMINDERS = "reminders"

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            val summaryChannel = NotificationChannel(
                TASK_SUMMARIES,
                "Task Summaries",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Daily task summary notifications"
            }

            val reminderChannel = NotificationChannel(
                REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Task reminder notifications"
            }

            manager.createNotificationChannels(listOf(summaryChannel, reminderChannel))
        }
    }
}
