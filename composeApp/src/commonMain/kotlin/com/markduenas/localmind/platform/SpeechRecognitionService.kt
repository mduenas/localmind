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

    fun startListening()
    fun stopListening()
    fun cancel()
}
