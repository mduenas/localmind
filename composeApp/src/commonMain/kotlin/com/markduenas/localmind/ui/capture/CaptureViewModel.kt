package com.markduenas.localmind.ui.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markduenas.localmind.ai.AIConfig
import com.markduenas.localmind.ai.ModelManager
import com.markduenas.localmind.ai.STTService
import com.markduenas.localmind.platform.AudioFileProvider
import com.markduenas.localmind.platform.AudioRecorder
import com.markduenas.localmind.platform.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CaptureUiState(
    val inputText: String = "",
    val isRecording: Boolean = false,
    val isTranscribing: Boolean = false,
    val error: String? = null,
    val needsMicPermission: Boolean = false,
)

class CaptureViewModel(
    private val sttService: STTService,
    private val audioRecorder: AudioRecorder,
    private val audioFileProvider: AudioFileProvider,
    private val permissionHelper: PermissionHelper,
    private val modelManager: ModelManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    private var currentAudioPath: String? = null

    fun onTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onSubmit(): String? {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return null
        return text
    }

    fun toggleRecording() {
        if (_uiState.value.isTranscribing) return

        if (_uiState.value.isRecording) {
            stopRecordingAndTranscribe()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        if (!permissionHelper.hasMicrophonePermission()) {
            _uiState.update { it.copy(needsMicPermission = true) }
            return
        }

        if (!modelManager.isModelDownloaded(AIConfig.DEFAULT_STT_MODEL)) {
            _uiState.update {
                it.copy(error = "Download the whisper-tiny model in Settings first")
            }
            return
        }

        try {
            val path = audioFileProvider.createTempAudioFile()
            currentAudioPath = path
            audioRecorder.startRecording(path)
            _uiState.update { it.copy(isRecording = true, error = null) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to start recording: ${e.message}") }
        }
    }

    private fun stopRecordingAndTranscribe() {
        try {
            audioRecorder.stopRecording()
        } catch (_: Exception) {
            // Best-effort stop
        }
        _uiState.update { it.copy(isRecording = false) }

        val audioPath = currentAudioPath ?: return
        currentAudioPath = null
        transcribe(audioPath)
    }

    private fun transcribe(audioPath: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isTranscribing = true, error = null) }
            try {
                sttService.initialize()
                val text = sttService.transcribe(audioPath)
                _uiState.update { it.copy(inputText = text, isTranscribing = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTranscribing = false,
                        error = "Transcription failed: ${e.message}",
                    )
                }
            } finally {
                audioFileProvider.deleteFile(audioPath)
            }
        }
    }

    fun onMicPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(needsMicPermission = false) }
        if (granted) {
            startRecording()
        } else {
            _uiState.update { it.copy(error = "Microphone permission is required for voice capture") }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        if (audioRecorder.isRecording()) {
            audioRecorder.stopRecording()
        }
        currentAudioPath?.let { audioFileProvider.deleteFile(it) }
    }
}
