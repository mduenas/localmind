package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.ai.InferenceLog
import com.markduenas.localmind.ai.LLMService
import com.markduenas.localmind.ai.ModelManager
import com.markduenas.localmind.ai.ParseOutput
import com.markduenas.localmind.ai.TaskParser
import com.markduenas.localmind.data.repository.SettingsRepository
import com.markduenas.localmind.domain.model.ParsedTask

class StubTaskParser(
    private val result: ParsedTask? = null,
    private val shouldThrow: Boolean = false,
) : TaskParser(LLMService(ModelManager(), SettingsRepository())) {

    override suspend fun parse(rawText: String): ParseOutput {
        if (shouldThrow) throw RuntimeException("LLM failed")
        val task = result ?: throw RuntimeException("No result configured")
        val log = InferenceLog(
            model = "stub",
            systemPrompt = "",
            userPrompt = rawText,
            rawResponse = "{}",
            durationMs = 0,
            method = "llm",
        )
        return ParseOutput(task, log)
    }
}
