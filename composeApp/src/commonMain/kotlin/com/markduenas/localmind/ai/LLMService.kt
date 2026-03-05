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
    private val modelManager: ModelManager,
    private val settingsRepository: com.markduenas.localmind.data.repository.SettingsRepository,
) {
    private var cactusLM: CactusLM? = null
    private var loadedModel: String? = null
    private val mutex = Mutex()

    val isLoaded: Boolean get() = cactusLM?.isLoaded() == true
    val currentModel: String? get() = loadedModel

    suspend fun initialize(model: String? = null) {
        val selectedModel = model
            ?: settingsRepository.selectedLlmModel.value.ifEmpty { AIConfig.DEFAULT_LLM_MODEL }

        mutex.withLock {
            // If already loaded with the requested model, skip
            if (cactusLM?.isLoaded() == true && loadedModel == selectedModel) return

            // Unload previous model if switching
            if (cactusLM?.isLoaded() == true && loadedModel != selectedModel) {
                cactusLM?.unload()
                cactusLM = null
                loadedModel = null
            }

            val lm = CactusLM()
            lm.downloadModel(selectedModel)
            lm.initializeModel(
                CactusInitParams(
                    model = selectedModel,
                    contextSize = AIConfig.CONTEXT_SIZE
                )
            )
            cactusLM = lm
            loadedModel = selectedModel
        }
    }

    suspend fun generateCompletion(
        systemPrompt: String,
        userPrompt: String,
        maxTokens: Int = AIConfig.MAX_TOKENS_MEDIUM_INPUT,
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
                            maxTokens = maxTokens,
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
