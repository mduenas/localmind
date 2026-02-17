package com.markduenas.localmind.ui.capture

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CaptureUiState(
    val inputText: String = "",
    val isRecording: Boolean = false,
    val isTranscribing: Boolean = false,
    val error: String? = null,
)

class CaptureViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    fun onTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onSubmit(): String? {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return null
        return text
    }

    fun toggleRecording() {
        _uiState.update { it.copy(isRecording = !it.isRecording) }
        // TODO: Integrate STTService for actual recording/transcription
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
