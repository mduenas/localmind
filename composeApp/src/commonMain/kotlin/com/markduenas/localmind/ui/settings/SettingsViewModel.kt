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
import com.markduenas.localmind.platform.PermissionHelper
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
    ),
    val selectedLlmModel: String = AIConfig.DEFAULT_LLM_MODEL,
    val downloadState: ModelDownloadState = ModelDownloadState.Idle,
    val error: String? = null,
    val needsNotificationPermission: Boolean = false,
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
    private val permissionHelper: PermissionHelper,
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    private val _downloadState = MutableStateFlow<ModelDownloadState>(ModelDownloadState.Idle)
    private val _needsNotificationPermission = MutableStateFlow(false)
    private var progressJob: Job? = null

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.llmEnabled,
        settingsRepository.notificationsEnabled,
        settingsRepository.selectedLlmModel,
        _error,
        _downloadState,
    ) { llm, notifications, selectedModel, error, downloadState ->
        val downloaded = modelManager.getDownloadedModels()
        val effectiveSelected = selectedModel.ifEmpty { AIConfig.DEFAULT_LLM_MODEL }
        SettingsUiState(
            llmEnabled = llm,
            notificationsEnabled = notifications,
            downloadedModels = downloaded,
            selectedLlmModel = effectiveSelected,
            downloadState = downloadState,
            error = error,
            needsNotificationPermission = _needsNotificationPermission.value,
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
        val modelsDir = try { modelManager.getModelsDirectory() } catch (_: Exception) { null }
        val baselineSize = modelsDir?.let { try { directorySize(it) } catch (_: Exception) { 0L } } ?: 0L

        progressJob?.cancel()
        if (modelsDir != null && expectedBytes != null && expectedBytes > 0) {
            progressJob = viewModelScope.launch {
                while (isActive) {
                    delay(500)
                    val currentSize = try { directorySize(modelsDir) - baselineSize } catch (_: Exception) { 0L }
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
                // Auto-select first downloaded LLM model if none selected
                if (slug !in AIConfig.STT_MODELS) {
                    val currentSelected = settingsRepository.selectedLlmModel.value
                    if (currentSelected.isEmpty() || !modelManager.isModelDownloaded(currentSelected)) {
                        settingsRepository.setSelectedLlmModel(slug)
                    }
                    settingsRepository.setLlmEnabled(true)
                }
                _downloadState.value = ModelDownloadState.Idle
            } catch (e: Exception) {
                progressJob?.cancel()
                // Show the root cause for debugging
                val rootCause = generateSequence<Throwable>(e) { it.cause }.last()
                val detail = if (rootCause !== e) "${e.message} (${rootCause::class.simpleName}: ${rootCause.message})" else e.message
                _downloadState.value = ModelDownloadState.Failed(
                    slug = slug,
                    error = detail ?: "Download failed",
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
        if (enabled) {
            if (permissionHelper.hasNotificationPermission()) {
                settingsRepository.setNotificationsEnabled(true)
                try {
                    notificationHelper.scheduleDailySummary()
                } catch (e: Exception) {
                    _error.update { e.message }
                }
            } else {
                _needsNotificationPermission.value = true
            }
        } else {
            settingsRepository.setNotificationsEnabled(false)
            try {
                notificationHelper.cancelAll()
            } catch (e: Exception) {
                _error.update { e.message }
            }
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        _needsNotificationPermission.value = false
        if (granted) {
            settingsRepository.setNotificationsEnabled(true)
            try {
                notificationHelper.scheduleDailySummary()
            } catch (e: Exception) {
                _error.update { e.message }
            }
        } else {
            settingsRepository.setNotificationsEnabled(false)
        }
    }

    fun selectModel(slug: String) {
        settingsRepository.setSelectedLlmModel(slug)
    }

    fun deleteModel(slug: String) {
        try {
            modelManager.deleteModel(slug)
            // If deleting the selected model, fall back to default or first available
            if (slug == settingsRepository.selectedLlmModel.value) {
                val remaining = modelManager.getDownloadedModels()
                    .filter { it !in AIConfig.STT_MODELS && it != slug }
                val fallback = remaining.firstOrNull() ?: AIConfig.DEFAULT_LLM_MODEL
                settingsRepository.setSelectedLlmModel(fallback)
            }
            // If no LLM models remain, disable LLM
            val hasLlm = modelManager.getDownloadedModels().any { it !in AIConfig.STT_MODELS }
            if (!hasLlm && settingsRepository.llmEnabled.value) {
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
