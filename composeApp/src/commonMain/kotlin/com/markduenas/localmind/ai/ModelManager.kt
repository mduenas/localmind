package com.markduenas.localmind.ai

import com.cactus.CactusLM
import com.cactus.CactusModelManager
import com.cactus.CactusSTT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ModelManager {

    fun isModelDownloaded(slug: String): Boolean {
        return CactusModelManager.isModelDownloaded(slug)
    }

    fun getDownloadedModels(): List<String> {
        return CactusModelManager.getDownloadedModels()
    }

    suspend fun ensureModel(slug: String): Boolean {
        if (isModelDownloaded(slug)) return true
        // Model needs to be downloaded — caller should trigger download with progress UI
        return false
    }

    /**
     * Downloads a model by creating a temporary CactusLM instance.
     * Keeps download separate from LLM initialization so the caller
     * can drive progress UI without committing to a loaded model.
     */
    suspend fun downloadModel(slug: String) {
        withContext(Dispatchers.IO) {
            try {
                if (slug in AIConfig.STT_MODELS) {
                    val stt = CactusSTT()
                    stt.downloadModel(slug)
                } else {
                    val lm = CactusLM()
                    lm.downloadModel(slug)
                }
            } catch (e: Exception) {
                throw RuntimeException(
                    "Failed to download model '$slug': ${e::class.simpleName}: ${e.message}",
                    e,
                )
            }
        }
    }

    fun deleteModel(slug: String): Boolean {
        return CactusModelManager.deleteModel(slug)
    }

    fun getModelsDirectory(): String {
        return CactusModelManager.getModelsDirectory()
    }
}

/**
 * Scans a directory tree and returns total size of all files in bytes.
 * Used to estimate download progress by comparing against expected model size.
 */
expect fun directorySize(path: String): Long
