package com.markduenas.localmind.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVAudioSessionRecordPermissionUndetermined
import platform.Speech.SFSpeechRecognizer

// SFSpeechRecognizerAuthorizationStatus raw values
private const val AUTH_NOT_DETERMINED = 0L
private const val AUTH_AUTHORIZED = 3L

@Composable
actual fun SpeechPermissionEffect(
    shouldRequest: Boolean,
    onResult: (granted: Boolean) -> Unit,
) {
    LaunchedEffect(shouldRequest) {
        if (!shouldRequest) return@LaunchedEffect

        val session = AVAudioSession.sharedInstance()
        when (session.recordPermission) {
            AVAudioSessionRecordPermissionGranted -> {
                requestSpeechPermission(onResult)
            }
            AVAudioSessionRecordPermissionUndetermined -> {
                session.requestRecordPermission { micGranted ->
                    if (micGranted) {
                        requestSpeechPermission(onResult)
                    } else {
                        onResult(false)
                    }
                }
            }
            else -> onResult(false)
        }
    }
}

private fun requestSpeechPermission(onResult: (Boolean) -> Unit) {
    val status = SFSpeechRecognizer.authorizationStatus()
    when (status.value) {
        AUTH_AUTHORIZED -> onResult(true)
        AUTH_NOT_DETERMINED -> {
            SFSpeechRecognizer.requestAuthorization { newStatus ->
                onResult(newStatus.value == AUTH_AUTHORIZED)
            }
        }
        else -> onResult(false)
    }
}
