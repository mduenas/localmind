package com.markduenas.localmind.platform

import androidx.compose.runtime.Composable

@Composable
actual fun SpeechActivityFallbackEffect(speechService: SpeechRecognitionService) {
    // No-op on iOS — SFSpeechRecognizer handles everything in-process
}
