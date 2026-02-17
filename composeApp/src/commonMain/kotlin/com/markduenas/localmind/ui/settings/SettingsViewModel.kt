package com.markduenas.localmind.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markduenas.localmind.ai.AIConfig
import com.markduenas.localmind.ai.ModelManager
import com.markduenas.localmind.data.repository.SettingsRepository
import com.markduenas.localmind.platform.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val llmEnabled: Boolean = false,
    val encryptionEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val downloadedModels: List<String> = emptyList(),
    val availableModels: List<String> = listOf(
        AIConfig.DEFAULT_LLM_MODEL,
        AIConfig.FALLBACK_LLM_MODEL,
        AIConfig.DEFAULT_STT_MODEL,
    ),
    val error: String? = null,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val modelManager: ModelManager,
    private val notificationHelper: NotificationHelper,
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.llmEnabled,
        settingsRepository.encryptionEnabled,
        settingsRepository.notificationsEnabled,
        _error,
    ) { llm, encryption, notifications, error ->
        SettingsUiState(
            llmEnabled = llm,
            encryptionEnabled = encryption,
            notificationsEnabled = notifications,
            downloadedModels = modelManager.getDownloadedModels(),
            error = error,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SettingsUiState(),
    )

    fun setLlmEnabled(enabled: Boolean) {
        settingsRepository.setLlmEnabled(enabled)
    }

    fun setEncryptionEnabled(enabled: Boolean) {
        settingsRepository.setEncryptionEnabled(enabled)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        settingsRepository.setNotificationsEnabled(enabled)
        try {
            if (enabled) {
                notificationHelper.scheduleDailySummary()
            } else {
                notificationHelper.cancelAll()
            }
        } catch (e: Exception) {
            _error.update { e.message }
        }
    }

    fun deleteModel(slug: String) {
        try {
            modelManager.deleteModel(slug)
        } catch (e: Exception) {
            _error.update { e.message }
        }
    }

    fun clearError() {
        _error.update { null }
    }
}
