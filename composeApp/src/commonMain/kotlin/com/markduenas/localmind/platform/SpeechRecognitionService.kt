package com.markduenas.localmind.platform

import kotlinx.coroutines.flow.StateFlow

data class SpeechResult(
    val text: String = "",
    val isFinal: Boolean = false,
    val error: String? = null,
    /** Optional label for an actionable follow-up, e.g. "Get one" to install a voice app. */
    val errorActionLabel: String? = null,
    /** URL to open (via LocalUriHandler) when [errorActionLabel] is tapped. */
    val errorActionUrl: String? = null,
)

expect class SpeechRecognitionService {
    val result: StateFlow<SpeechResult>
    val isListening: StateFlow<Boolean>

    /** Whether this device has a usable speech recognizer, checked before starting. */
    fun isAvailable(): Boolean

    /** The (platform-appropriate) message/action to show when [isAvailable] is false. */
    fun unavailableResult(): SpeechResult

    fun startListening()
    fun stopListening()
    fun cancel()
}
