package com.markduenas.localmind.platform

import androidx.compose.runtime.Composable

/**
 * On Android, observes [SpeechRecognitionService.pendingRecognitionIntent] and launches
 * the recognizer as an activity when the service-based approach fails.
 * No-op on iOS (SFSpeechRecognizer doesn't need this fallback).
 */
@Composable
expect fun SpeechActivityFallbackEffect(speechService: SpeechRecognitionService)
