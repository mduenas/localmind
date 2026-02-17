package com.markduenas.localmind.platform

import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents

actual class NotificationHelper {
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    actual fun scheduleDailySummary(hour: Int, minute: Int) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Today's Tasks")
            setBody("Check your tasks for today")
        }

        val dateComponents = NSDateComponents().apply {
            setHour(hour.toLong())
            setMinute(minute.toLong())
        }

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents,
            repeats = true,
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            "daily_summary",
            content,
            trigger,
        )

        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    actual fun cancelAll() {
        center.removeAllPendingNotificationRequests()
    }
}
