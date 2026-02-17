package com.markduenas.localmind.ai

import com.cactus.ChatMessage
import com.cactus.CactusCompletionParams
import com.cactus.CactusInitParams
import com.cactus.CactusLM
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout

class LLMService(
    private val modelManager: ModelManager
) {
    private var cactusLM: CactusLM? = null
    private val mutex = Mutex()

    val isLoaded: Boolean get() = cactusLM?.isLoaded() == true

    suspend fun initialize(model: String = AIConfig.DEFAULT_LLM_MODEL) {
        mutex.withLock {
            if (cactusLM?.isLoaded() == true) return

            val lm = CactusLM()
            lm.downloadModel(model)
            lm.initializeModel(
                CactusInitParams(
                    model = model,
                    contextSize = AIConfig.CONTEXT_SIZE
                )
            )
            cactusLM = lm
        }
    }

    suspend fun generateCompletion(
        systemPrompt: String,
        userPrompt: String
    ): String {
        val lm = cactusLM ?: throw IllegalStateException("LLM not initialized — call initialize() first")

        val messages = listOf(
            ChatMessage(content = systemPrompt, role = "system"),
            ChatMessage(content = userPrompt, role = "user")
        )

        var lastException: Exception? = null
        repeat(2) { attempt ->
            try {
                val result = withTimeout(AIConfig.LLM_TIMEOUT_MS) {
                    lm.generateCompletion(
                        messages = messages,
                        params = CactusCompletionParams(
                            maxTokens = AIConfig.MAX_TOKENS,
                            temperature = AIConfig.TEMPERATURE
                        )
                    )
                }

                if (result == null || !result.success) {
                    throw LLMException("LLM completion failed")
                }

                return result.response ?: throw LLMException("LLM returned empty response")
            } catch (e: Exception) {
                lastException = e
                if (attempt < 1) delay(500)
            }
        }

        throw lastException ?: LLMException("LLM completion failed after retries")
    }

    fun unload() {
        cactusLM?.unload()
        cactusLM = null
    }
}

class LLMException(message: String, cause: Throwable? = null) : Exception(message, cause)
