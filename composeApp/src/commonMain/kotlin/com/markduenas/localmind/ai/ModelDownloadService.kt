package com.markduenas.localmind.ai

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface ModelDownloadState {
    data object Idle : ModelDownloadState
    data class Downloading(val slug: String, val progress: Float) : ModelDownloadState
    data class Failed(val slug: String, val error: String) : ModelDownloadState
}

class ModelDownloadService(
    private val modelManager: ModelManager,
    private val backgroundTaskRunner: BackgroundTaskRunner,
) {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow<ModelDownloadState>(ModelDownloadState.Idle)
    val state: StateFlow<ModelDownloadState> = _state.asStateFlow()

    private var progressJob: Job? = null

    fun startDownload(slug: String) {
        if (_state.value is ModelDownloadState.Downloading) return
        if (modelManager.isModelDownloaded(slug)) return

        _state.value = ModelDownloadState.Downloading(slug, progress = -1f)

        val expectedBytes = AIConfig.MODEL_BYTES[slug]
        val modelsDir = try { modelManager.getModelsDirectory() } catch (_: Exception) { null }
        val baselineSize = modelsDir?.let { try { directorySize(it) } catch (_: Exception) { 0L } } ?: 0L

        progressJob?.cancel()
        if (modelsDir != null && expectedBytes != null && expectedBytes > 0) {
            progressJob = serviceScope.launch {
                while (isActive) {
                    delay(500)
                    val currentSize = try { directorySize(modelsDir) - baselineSize } catch (_: Exception) { 0L }
                    val pct = (currentSize.toFloat() / expectedBytes).coerceIn(0f, 0.99f)
                    _state.value = ModelDownloadState.Downloading(slug, progress = pct)
                }
            }
        }

        serviceScope.launch {
            try {
                backgroundTaskRunner.runInBackground {
                    modelManager.downloadModel(slug)
                }
                progressJob?.cancel()
                _state.value = ModelDownloadState.Idle
            } catch (e: Throwable) {
                progressJob?.cancel()
                val rootCause = generateSequence<Throwable>(e) { it.cause }.last()
                val detail = if (rootCause !== e) {
                    "${e.message} (${rootCause::class.simpleName}: ${rootCause.message})"
                } else {
                    e.message
                }
                _state.value = ModelDownloadState.Failed(
                    slug = slug,
                    error = detail ?: "Download failed",
                )
            }
        }
    }

    fun retryDownload() {
        val failed = _state.value as? ModelDownloadState.Failed ?: return
        startDownload(failed.slug)
    }

    fun dismissError() {
        _state.value = ModelDownloadState.Idle
    }
}
