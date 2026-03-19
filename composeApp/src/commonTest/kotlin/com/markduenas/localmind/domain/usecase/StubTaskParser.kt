package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.ai.InferenceLog
import com.markduenas.localmind.ai.ParseOutput
import com.markduenas.localmind.ai.TaskParser
import com.markduenas.localmind.domain.model.ParsedCapture
import com.markduenas.localmind.domain.model.ParsedTask

class StubTaskParser(
    private val result: ParsedTask? = null,
    private val shouldThrow: Boolean = false,
) : TaskParser(null) {
    var parseCallCount: Int = 0
        private set

    override suspend fun parse(rawText: String): ParseOutput {
        parseCallCount++
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
        return ParseOutput(ParsedCapture.TaskCapture(task), log)
    }
}
