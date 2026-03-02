package com.markduenas.localmind.platform

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun NotificationPermissionEffect(
    shouldRequest: Boolean,
    onResult: (granted: Boolean) -> Unit,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        LaunchedEffect(shouldRequest) {
            if (shouldRequest) {
                onResult(true)
            }
        }
        return
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onResult(granted)
    }

    LaunchedEffect(shouldRequest) {
        if (shouldRequest) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
