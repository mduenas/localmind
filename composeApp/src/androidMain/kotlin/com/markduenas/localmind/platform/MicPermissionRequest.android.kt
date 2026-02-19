package com.markduenas.localmind.platform

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun MicPermissionEffect(
    shouldRequest: Boolean,
    onResult: (granted: Boolean) -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onResult(granted)
    }

    LaunchedEffect(shouldRequest) {
        if (shouldRequest) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}
