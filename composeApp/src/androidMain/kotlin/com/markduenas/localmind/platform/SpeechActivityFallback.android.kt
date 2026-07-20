package com.markduenas.localmind.platform

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
actual fun SpeechActivityFallbackEffect(speechService: SpeechRecognitionService) {
    val pendingIntent by speechService.pendingRecognitionIntent.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val text = if (result.resultCode == Activity.RESULT_OK) {
            result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
        } else {
            null
        }
        speechService.onActivityResult(text)
    }

    LaunchedEffect(pendingIntent) {
        pendingIntent?.let { intent ->
            try {
                launcher.launch(intent)
            } catch (_: ActivityNotFoundException) {
                speechService.onActivityLaunchFailed()
            }
        }
    }
}
