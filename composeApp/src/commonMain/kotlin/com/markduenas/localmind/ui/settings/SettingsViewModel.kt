package com.markduenas.localmind.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markduenas.localmind.ai.AIConfig
import com.markduenas.localmind.ai.ModelManager
import com.markduenas.localmind.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val modelManager: ModelManager,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.llmEnabled,
        settingsRepository.encryptionEnabled,
        settingsRepository.notificationsEnabled,
    ) { llm, encryption, notifications ->
        SettingsUiState(
            llmEnabled = llm,
            encryptionEnabled = encryption,
            notificationsEnabled = notifications,
            downloadedModels = modelManager.getDownloadedModels(),
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
    }

    fun deleteModel(slug: String) {
        modelManager.deleteModel(slug)
    }
}
