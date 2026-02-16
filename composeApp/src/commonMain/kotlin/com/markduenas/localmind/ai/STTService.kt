package com.markduenas.localmind.ai

import com.cactus.CactusInitParams
import com.cactus.CactusSTT
import com.cactus.CactusTranscriptionParams
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout

class STTService(
    private val modelManager: ModelManager
) {
    private var cactusSTT: CactusSTT? = null
    private val mutex = Mutex()

    val isReady: Boolean get() = cactusSTT?.isReady() == true

    suspend fun initialize(model: String = AIConfig.DEFAULT_STT_MODEL) {
        mutex.withLock {
            if (cactusSTT?.isReady() == true) return

            val stt = CactusSTT()
            stt.downloadModel(model)
            stt.initializeModel(CactusInitParams(model = model))
            cactusSTT = stt
        }
    }

    suspend fun transcribe(audioFilePath: String): String {
        val stt = cactusSTT ?: throw IllegalStateException("STT not initialized — call initialize() first")

        val result = withTimeout(AIConfig.STT_TIMEOUT_MS) {
            stt.transcribe(filePath = audioFilePath)
        }

        if (result == null || !result.success) {
            throw STTException(result?.errorMessage ?: "Transcription failed")
        }

        return result.text?.trim() ?: throw STTException("Transcription returned empty text")
    }

    fun reset() {
        cactusSTT?.reset()
        cactusSTT = null
    }
}

class STTException(message: String, cause: Throwable? = null) : Exception(message, cause)
