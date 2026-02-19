package com.markduenas.localmind.ai

import com.cactus.CactusLM
import com.cactus.CactusModelManager

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
        try {
            val lm = CactusLM()
            lm.downloadModel(slug)
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to download model '$slug'. Check your internet connection and try again.",
                e,
            )
        }
    }

    fun deleteModel(slug: String): Boolean {
        return CactusModelManager.deleteModel(slug)
    }

    fun getModelsDirectory(): String {
        return CactusModelManager.getModelsDirectory()
    }
}
