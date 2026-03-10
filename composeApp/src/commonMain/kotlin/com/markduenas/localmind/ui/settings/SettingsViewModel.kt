package com.markduenas.localmind.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markduenas.localmind.ai.AIConfig
import com.markduenas.localmind.ai.ModelDownloadService
import com.markduenas.localmind.ai.ModelDownloadState
import com.markduenas.localmind.ai.ModelManager
import com.markduenas.localmind.data.repository.BillingRepository
import com.markduenas.localmind.data.repository.SettingsRepository
import com.markduenas.localmind.data.repository.TaskRepository
import com.markduenas.localmind.platform.FileSharer
import com.markduenas.localmind.platform.NotificationHelper
import com.markduenas.localmind.platform.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class SettingsUiState(
    val llmEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val downloadedModels: List<String> = emptyList(),
    val availableModels: List<String> = listOf(
        AIConfig.DEFAULT_LLM_MODEL,
        AIConfig.FUNCTION_TINY_LLM_MODEL,
    ),
    val selectedLlmModel: String = AIConfig.DEFAULT_LLM_MODEL,
    val downloadState: ModelDownloadState = ModelDownloadState.Idle,
    val error: String? = null,
    val needsNotificationPermission: Boolean = false,
    val isPremium: Boolean = false,
    val products: List<com.markduenas.localmind.billing.BillingProduct> = emptyList(),
    val showPaywall: Boolean = false,
    val purchaseInProgress: Boolean = false,
    val restoreInProgress: Boolean = false,
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
    private val billingRepository: BillingRepository,
    private val modelDownloadService: ModelDownloadService,
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    private val _needsNotificationPermission = MutableStateFlow(false)
    private val _showPaywall = MutableStateFlow(false)
    private val _purchaseInProgress = MutableStateFlow(false)
    private val _restoreInProgress = MutableStateFlow(false)

    init {
        // Auto-enable LLM when download completes
        viewModelScope.launch {
            modelDownloadService.state.collect { downloadState ->
                if (downloadState is ModelDownloadState.Idle) {
                    // Check if a model just finished downloading (download service transitions to Idle on success)
                    val downloaded = modelManager.getDownloadedModels().filter { it !in AIConfig.STT_MODELS }
                    if (downloaded.isNotEmpty() && !settingsRepository.llmEnabled.value) {
                        val currentSelected = settingsRepository.selectedLlmModel.value
                        if (currentSelected.isEmpty() || !modelManager.isModelDownloaded(currentSelected)) {
                            settingsRepository.setSelectedLlmModel(downloaded.first())
                        }
                        settingsRepository.setLlmEnabled(true)
                    }
                }
            }
        }
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.llmEnabled,
        settingsRepository.notificationsEnabled,
        settingsRepository.selectedLlmModel,
        _error,
        modelDownloadService.state,
        _needsNotificationPermission,
        settingsRepository.premiumActive,
        billingRepository.products,
        _showPaywall,
        _purchaseInProgress,
        _restoreInProgress,
    ) { values ->
        val llm = values[0] as Boolean
        val notifications = values[1] as Boolean
        val selectedModel = values[2] as String
        val error = values[3] as String?
        val downloadState = values[4] as ModelDownloadState
        val needsPermission = values[5] as Boolean
        val isPremium = values[6] as Boolean
        @Suppress("UNCHECKED_CAST")
        val products = values[7] as List<com.markduenas.localmind.billing.BillingProduct>
        val showPaywall = values[8] as Boolean
        val purchaseInProgress = values[9] as Boolean
        val restoreInProgress = values[10] as Boolean

        val downloaded = modelManager.getDownloadedModels()
        val effectiveSelected = selectedModel.ifEmpty { AIConfig.DEFAULT_LLM_MODEL }
        SettingsUiState(
            llmEnabled = llm,
            notificationsEnabled = notifications,
            downloadedModels = downloaded,
            selectedLlmModel = effectiveSelected,
            downloadState = downloadState,
            error = error,
            needsNotificationPermission = needsPermission,
            isPremium = isPremium,
            products = products,
            showPaywall = showPaywall,
            purchaseInProgress = purchaseInProgress,
            restoreInProgress = restoreInProgress,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SettingsUiState(),
    )

    fun setLlmEnabled(enabled: Boolean) {
        if (modelDownloadService.state.value is ModelDownloadState.Downloading) return

        if (enabled) {
            if (!settingsRepository.premiumActive.value) {
                _showPaywall.value = true
                return
            }
            val defaultModel = AIConfig.DEFAULT_LLM_MODEL
            if (modelManager.isModelDownloaded(defaultModel)) {
                settingsRepository.setLlmEnabled(true)
            } else {
                modelDownloadService.startDownload(defaultModel)
            }
        } else {
            settingsRepository.setLlmEnabled(false)
        }
    }

    fun requestModelDownload(slug: String) {
        modelDownloadService.startDownload(slug)
    }

    fun retryDownload() {
        modelDownloadService.retryDownload()
    }

    fun dismissDownloadError() {
        modelDownloadService.dismissError()
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
            if (slug == settingsRepository.selectedLlmModel.value) {
                val remaining = modelManager.getDownloadedModels()
                    .filter { it !in AIConfig.STT_MODELS && it != slug }
                val fallback = remaining.firstOrNull() ?: AIConfig.DEFAULT_LLM_MODEL
                settingsRepository.setSelectedLlmModel(fallback)
            }
            val hasLlm = modelManager.getDownloadedModels().any { it !in AIConfig.STT_MODELS }
            if (!hasLlm && settingsRepository.llmEnabled.value) {
                settingsRepository.setLlmEnabled(false)
            }
        } catch (e: Exception) {
            _error.update { e.message }
        }
    }

    fun exportTasks() {
        if (!settingsRepository.premiumActive.value) {
            _showPaywall.value = true
            return
        }
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

    fun showPaywall() {
        _showPaywall.value = true
    }

    fun dismissPaywall() {
        _showPaywall.value = false
    }

    fun purchaseProduct(productId: String) {
        viewModelScope.launch {
            _purchaseInProgress.value = true
            try {
                val result = billingRepository.purchase(productId)
                when (result) {
                    is com.markduenas.localmind.billing.PurchaseResult.Success,
                    is com.markduenas.localmind.billing.PurchaseResult.AlreadyOwned -> {
                        _showPaywall.value = false
                    }
                    is com.markduenas.localmind.billing.PurchaseResult.Cancelled -> {
                        // User cancelled — keep paywall open
                    }
                    is com.markduenas.localmind.billing.PurchaseResult.Error -> {
                        _error.update { result.message }
                    }
                }
            } catch (e: Exception) {
                _error.update { "Purchase failed: ${e.message}" }
            } finally {
                _purchaseInProgress.value = false
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _restoreInProgress.value = true
            try {
                val restored = billingRepository.restorePurchases()
                if (restored) {
                    _showPaywall.value = false
                } else {
                    _error.update { "No previous purchases found" }
                }
            } catch (e: Exception) {
                _error.update { "Restore failed: ${e.message}" }
            } finally {
                _restoreInProgress.value = false
            }
        }
    }

    fun clearError() {
        _error.update { null }
    }
}
