package com.markduenas.localmind.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markduenas.localmind.ai.AIConfig
import com.markduenas.localmind.ai.ModelManager
import com.markduenas.localmind.data.repository.SettingsRepository
import com.markduenas.localmind.data.repository.TaskRepository
import com.markduenas.localmind.platform.FileSharer
import com.markduenas.localmind.platform.NotificationHelper
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed interface ModelDownloadState {
    data object Idle : ModelDownloadState
    data class ConfirmationRequired(val slug: String) : ModelDownloadState
    data class Downloading(val slug: String) : ModelDownloadState
    data class Failed(val slug: String, val error: String) : ModelDownloadState
}

data class SettingsUiState(
    val llmEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val downloadedModels: List<String> = emptyList(),
    val availableModels: List<String> = listOf(
        AIConfig.DEFAULT_LLM_MODEL,
        AIConfig.FALLBACK_LLM_MODEL,
        AIConfig.DEFAULT_STT_MODEL,
    ),
    val downloadState: ModelDownloadState = ModelDownloadState.Idle,
    val error: String? = null,
)

@Serializable
private data class ExportTask(
    val id: String,
    val title: String,
    val originalText: String,
    val dueDate: String?,
    val dueTime: String?,
    val priority: String,
    val status: String,
    val tags: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val completedAt: String?,
)

private val exportJson = Json { prettyPrint = true }

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val modelManager: ModelManager,
    private val notificationHelper: NotificationHelper,
    private val taskRepository: TaskRepository,
    private val fileSharer: FileSharer,
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    private val _downloadState = MutableStateFlow<ModelDownloadState>(ModelDownloadState.Idle)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.llmEnabled,
        settingsRepository.notificationsEnabled,
        _error,
        _downloadState,
    ) { llm, notifications, error, downloadState ->
        SettingsUiState(
            llmEnabled = llm,
            notificationsEnabled = notifications,
            downloadedModels = modelManager.getDownloadedModels(),
            downloadState = downloadState,
            error = error,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SettingsUiState(),
    )

    fun setLlmEnabled(enabled: Boolean) {
        // Block toggle while a download is in progress
        if (_downloadState.value is ModelDownloadState.Downloading) return

        if (enabled) {
            val defaultModel = AIConfig.DEFAULT_LLM_MODEL
            if (modelManager.isModelDownloaded(defaultModel)) {
                settingsRepository.setLlmEnabled(true)
            } else {
                _downloadState.value = ModelDownloadState.ConfirmationRequired(defaultModel)
            }
        } else {
            settingsRepository.setLlmEnabled(false)
        }
    }

    fun requestModelDownload(slug: String) {
        if (_downloadState.value is ModelDownloadState.Downloading) return
        if (modelManager.isModelDownloaded(slug)) return
        _downloadState.value = ModelDownloadState.ConfirmationRequired(slug)
    }

    fun confirmDownload() {
        val current = _downloadState.value
        val slug = when (current) {
            is ModelDownloadState.ConfirmationRequired -> current.slug
            is ModelDownloadState.Failed -> current.slug
            else -> return
        }
        _downloadState.value = ModelDownloadState.Downloading(slug)
        viewModelScope.launch {
            try {
                withContext(NonCancellable) {
                    modelManager.downloadModel(slug)
                }
                // If the LLM toggle triggered this download, enable LLM
                if (slug == AIConfig.DEFAULT_LLM_MODEL) {
                    settingsRepository.setLlmEnabled(true)
                }
                _downloadState.value = ModelDownloadState.Idle
            } catch (e: Exception) {
                _downloadState.value = ModelDownloadState.Failed(
                    slug = slug,
                    error = e.message ?: "Download failed",
                )
            }
        }
    }

    fun cancelDownload() {
        _downloadState.value = ModelDownloadState.Idle
    }

    fun dismissDownloadError() {
        _downloadState.value = ModelDownloadState.Idle
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
            // If the deleted model is the default LLM model, disable LLM
            if (slug == AIConfig.DEFAULT_LLM_MODEL && settingsRepository.llmEnabled.value) {
                settingsRepository.setLlmEnabled(false)
            }
        } catch (e: Exception) {
            _error.update { e.message }
        }
    }

    fun exportTasks() {
        viewModelScope.launch {
            try {
                val tasks = taskRepository.getAllTasks().first()
                val exportTasks = tasks.map { task ->
                    ExportTask(
                        id = task.id,
                        title = task.title,
                        originalText = task.originalText,
                        dueDate = task.dueDate?.toString(),
                        dueTime = task.dueTime?.toString(),
                        priority = task.priority.name,
                        status = task.status.name,
                        tags = task.tags.map { it.name },
                        createdAt = task.createdAt.toString(),
                        updatedAt = task.updatedAt.toString(),
                        completedAt = task.completedAt?.toString(),
                    )
                }
                val json = exportJson.encodeToString(exportTasks)
                fileSharer.share("localmind-tasks.json", json)
            } catch (e: Exception) {
                _error.update { "Export failed: ${e.message}" }
            }
        }
    }

    fun clearError() {
        _error.update { null }
    }
}
