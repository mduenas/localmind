package com.markduenas.localmind.platform

import kotlinx.coroutines.flow.StateFlow

data class SpeechResult(
    val text: String = "",
    val isFinal: Boolean = false,
    val error: String? = null,
)

expect class SpeechRecognitionService {
    val result: StateFlow<SpeechResult>
    val isListening: StateFlow<Boolean>

    /** Whether this device has a usable speech recognizer, checked before starting. */
    fun isAvailable(): Boolean

    fun startListening()
    fun stopListening()
    fun cancel()
}
