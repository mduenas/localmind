package com.markduenas.localmind.ui.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markduenas.localmind.platform.PermissionHelper
import com.markduenas.localmind.platform.SpeechRecognitionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CaptureUiState(
    val inputText: String = "",
    val isRecording: Boolean = false,
    val error: String? = null,
    val needsSpeechPermission: Boolean = false,
)

class CaptureViewModel(
    private val speechService: SpeechRecognitionService,
    private val permissionHelper: PermissionHelper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            speechService.isListening.collect { listening ->
                _uiState.update { it.copy(isRecording = listening) }
            }
        }
        viewModelScope.launch {
            speechService.result.collect { result ->
                if (result.text.isNotEmpty()) {
                    _uiState.update { it.copy(inputText = result.text) }
                }
                result.error?.let { error ->
                    _uiState.update { it.copy(error = error) }
                }
            }
        }
    }

    fun onTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onSubmit(): String? {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return null
        return text
    }

    fun toggleRecording() {
        if (_uiState.value.isRecording) {
            speechService.stopListening()
        } else {
            startListening()
        }
    }

    private fun startListening() {
        if (!permissionHelper.hasMicrophonePermission() || !permissionHelper.hasSpeechRecognitionPermission()) {
            _uiState.update { it.copy(needsSpeechPermission = true) }
            return
        }

        _uiState.update { it.copy(error = null) }
        speechService.startListening()
    }

    fun onSpeechPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(needsSpeechPermission = false) }
        if (granted) {
            startListening()
        } else {
            _uiState.update { it.copy(error = "Microphone and speech recognition permissions are required for voice capture") }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        speechService.cancel()
    }
}
