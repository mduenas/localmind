package com.markduenas.localmind.platform

import androidx.compose.runtime.Composable

@Composable
expect fun NotificationPermissionEffect(
    shouldRequest: Boolean,
    onResult: (granted: Boolean) -> Unit,
)
