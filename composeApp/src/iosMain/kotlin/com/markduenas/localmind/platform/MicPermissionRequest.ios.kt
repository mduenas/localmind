package com.markduenas.localmind.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVAudioSessionRecordPermissionUndetermined

@Composable
actual fun MicPermissionEffect(
    shouldRequest: Boolean,
    onResult: (granted: Boolean) -> Unit,
) {
    LaunchedEffect(shouldRequest) {
        if (!shouldRequest) return@LaunchedEffect

        val session = AVAudioSession.sharedInstance()
        when (session.recordPermission) {
            AVAudioSessionRecordPermissionGranted -> onResult(true)
            AVAudioSessionRecordPermissionUndetermined -> {
                session.requestRecordPermission { granted ->
                    onResult(granted)
                }
            }
            else -> onResult(false)
        }
    }
}
