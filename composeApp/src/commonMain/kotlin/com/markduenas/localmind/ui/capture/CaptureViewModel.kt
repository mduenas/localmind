package com.markduenas.localmind.ui.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markduenas.localmind.data.repository.SettingsRepository
import com.markduenas.localmind.domain.usecase.EnqueueCaptureUseCase
import com.markduenas.localmind.platform.PermissionHelper
import com.markduenas.localmind.platform.SpeechRecognitionService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CaptureUiState(
    val inputText: String = "",
    val isRecording: Boolean = false,
    val error: String? = null,
    val needsSpeechPermission: Boolean = false,
    val defaultToTextCapture: Boolean = false,
)

class CaptureViewModel(
    private val speechService: SpeechRecognitionService,
    private val permissionHelper: PermissionHelper,
    private val enqueueCapture: EnqueueCaptureUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CaptureUiState(
            defaultToTextCapture = settingsRepository.defaultToTextCapture.value ||
                !speechService.isAvailable(),
        )
    )
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()
    private val _captured = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val captured: SharedFlow<Unit> = _captured.asSharedFlow()

    init {
        viewModelScope.launch {
            speechService.isListening.collect { listening ->
                _uiState.update { it.copy(isRecording = listening) }
            }
        }
        viewModelScope.launch {
            // speechService.result is a shared StateFlow that retains its last value
            // (e.g. the final transcript from a previous capture). Drop that stale
            // replay so a fresh capture screen doesn't immediately resubmit it.
            speechService.result.drop(1).collect { result ->
                if (result.text.isNotEmpty()) {
                    _uiState.update { it.copy(inputText = result.text) }
                }
                if (result.isFinal && result.error == null && result.text.isNotBlank()) {
                    submit(result.text.trim())
                }
                result.error?.let { error ->
                    _uiState.update { it.copy(error = error) }
                }
            }
        }

        if (!_uiState.value.defaultToTextCapture) {
            startListening()
        }
    }

    fun onTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun submit() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return
        submit(text)
    }

    private fun submit(text: String) {
        viewModelScope.launch {
            enqueueCapture(text)
            _uiState.update { it.copy(inputText = "") }
            _captured.tryEmit(Unit)
        }
    }

    fun toggleRecording() {
        if (_uiState.value.isRecording) {
            speechService.stopListening()
        } else {
            startListening()
        }
    }

    private fun startListening() {
        if (!speechService.isAvailable()) {
            _uiState.update {
                it.copy(error = "Voice input isn't set up on this device. Use text capture instead.")
            }
            return
        }

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
