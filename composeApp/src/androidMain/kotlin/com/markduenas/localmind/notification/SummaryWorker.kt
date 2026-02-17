package com.markduenas.localmind.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.markduenas.localmind.MainActivity
import com.markduenas.localmind.data.repository.TaskRepository
import kotlinx.coroutines.flow.first

class SummaryWorker(
    private val context: Context,
    params: WorkerParameters,
    private val taskRepository: TaskRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val tasks = taskRepository.getTodayTasks().first()
        if (tasks.isEmpty()) return Result.success()

        val pendingCount = tasks.count { it.status == com.markduenas.localmind.domain.model.TaskStatus.PENDING }
        val completedCount = tasks.count { it.status == com.markduenas.localmind.domain.model.TaskStatus.COMPLETED }

        val title = "Today's Tasks"
        val body = when {
            pendingCount == 0 -> "All $completedCount tasks completed!"
            else -> "$pendingCount tasks remaining, $completedCount completed"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data = android.net.Uri.parse("localmind://today")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.TASK_SUMMARIES)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(SUMMARY_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Notification permission not granted
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "daily_summary"
        private const val SUMMARY_NOTIFICATION_ID = 1001
    }
}
