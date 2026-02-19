package com.markduenas.localmind.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markduenas.localmind.ai.AIConfig
import com.markduenas.localmind.ai.ModelManager
import com.markduenas.localmind.ai.directorySize
import com.markduenas.localmind.data.repository.SettingsRepository
import com.markduenas.localmind.data.repository.TaskRepository
import com.markduenas.localmind.platform.FileSharer
import com.markduenas.localmind.platform.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed interface ModelDownloadState {
    data object Idle : ModelDownloadState
    data class Downloading(val slug: String, val progress: Float) : ModelDownloadState
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
    private var progressJob: Job? = null

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
        if (_downloadState.value is ModelDownloadState.Downloading) return

        if (enabled) {
            val defaultModel = AIConfig.DEFAULT_LLM_MODEL
            if (modelManager.isModelDownloaded(defaultModel)) {
                settingsRepository.setLlmEnabled(true)
            } else {
                startDownload(defaultModel)
            }
        } else {
            settingsRepository.setLlmEnabled(false)
        }
    }

    fun requestModelDownload(slug: String) {
        if (_downloadState.value is ModelDownloadState.Downloading) return
        if (modelManager.isModelDownloaded(slug)) return
        startDownload(slug)
    }

    private fun startDownload(slug: String) {
        _downloadState.value = ModelDownloadState.Downloading(slug, progress = -1f)

        // Poll the models directory to estimate progress
        val expectedBytes = AIConfig.MODEL_BYTES[slug]
        val modelsDir = modelManager.getModelsDirectory()
        val baselineSize = directorySize(modelsDir)

        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                delay(500)
                if (expectedBytes != null && expectedBytes > 0) {
                    val currentSize = directorySize(modelsDir) - baselineSize
                    val pct = (currentSize.toFloat() / expectedBytes).coerceIn(0f, 0.99f)
                    _downloadState.value = ModelDownloadState.Downloading(slug, progress = pct)
                }
            }
        }

        viewModelScope.launch {
            try {
                withContext(NonCancellable) {
                    modelManager.downloadModel(slug)
                }
                progressJob?.cancel()
                if (slug == AIConfig.DEFAULT_LLM_MODEL) {
                    settingsRepository.setLlmEnabled(true)
                }
                _downloadState.value = ModelDownloadState.Idle
            } catch (e: Exception) {
                progressJob?.cancel()
                _downloadState.value = ModelDownloadState.Failed(
                    slug = slug,
                    error = e.message ?: "Download failed",
                )
            }
        }
    }

    fun retryDownload() {
        val failed = _downloadState.value as? ModelDownloadState.Failed ?: return
        startDownload(failed.slug)
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
