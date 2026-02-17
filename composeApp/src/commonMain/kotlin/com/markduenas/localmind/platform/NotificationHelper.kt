package com.markduenas.localmind.platform

expect class NotificationHelper {
    fun scheduleDailySummary(hour: Int = 9, minute: Int = 0)
    fun cancelAll()
}
