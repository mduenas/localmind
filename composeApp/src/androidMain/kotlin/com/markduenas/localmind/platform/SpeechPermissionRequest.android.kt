package com.markduenas.localmind.platform

import androidx.compose.runtime.Composable

@Composable
actual fun SpeechPermissionEffect(
    shouldRequest: Boolean,
    onResult: (granted: Boolean) -> Unit,
) {
    // On Android, speech recognition only needs RECORD_AUDIO — delegate to mic permission
    MicPermissionEffect(shouldRequest = shouldRequest, onResult = onResult)
}
