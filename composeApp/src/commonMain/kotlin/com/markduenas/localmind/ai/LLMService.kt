package com.markduenas.localmind.ai

import com.cactus.ChatMessage
import com.cactus.CactusCompletionParams
import com.cactus.CactusInitParams
import com.cactus.CactusLM
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
            if (!modelManager.isModelDownloaded(selectedModel)) {
                lm.downloadModel(selectedModel)
            }
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
        systemPrompt: String?,
        userPrompt: String,
        maxTokens: Int = AIConfig.MAX_TOKENS_MEDIUM_INPUT,
    ): String {
        val lm = cactusLM ?: throw IllegalStateException("LLM not initialized — call initialize() first")

        val messages = buildList {
            if (systemPrompt != null) add(ChatMessage(content = systemPrompt, role = "system"))
            add(ChatMessage(content = userPrompt, role = "user"))
        }

        val result = withTimeout(AIConfig.timeoutMsForModel(loadedModel)) {
            lm.generateCompletion(
                messages = messages,
                params = CactusCompletionParams(
                    maxTokens = maxTokens,
                    temperature = AIConfig.TEMPERATURE,
                    stopSequences = listOf("}")
                )
            )
        }

        if (result == null || !result.success) {
            throw LLMException("LLM completion failed")
        }

        // stopSequences strips the closing brace — reappend it so JSON parsing succeeds
        val raw = result.response ?: throw LLMException("LLM returned empty response")
        return if (raw.trimEnd().endsWith("}")) raw else "$raw}"
    }

    fun unload() {
        cactusLM?.unload()
        cactusLM = null
    }
}

class LLMException(message: String, cause: Throwable? = null) : Exception(message, cause)
