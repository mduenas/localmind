package com.markduenas.localmind.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter

@Composable
actual fun NotificationPermissionEffect(
    shouldRequest: Boolean,
    onResult: (granted: Boolean) -> Unit,
) {
    LaunchedEffect(shouldRequest) {
        if (!shouldRequest) return@LaunchedEffect

        val center = UNUserNotificationCenter.currentNotificationCenter()
        val options = UNAuthorizationOptionAlert or
            UNAuthorizationOptionSound or
            UNAuthorizationOptionBadge

        center.requestAuthorizationWithOptions(options) { granted, _ ->
            onResult(granted)
        }
    }
}
