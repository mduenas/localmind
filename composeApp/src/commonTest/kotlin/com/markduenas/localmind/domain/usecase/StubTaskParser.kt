package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.ai.LLMService
import com.markduenas.localmind.ai.ModelManager
import com.markduenas.localmind.ai.TaskParser
import com.markduenas.localmind.domain.model.ParsedTask

class StubTaskParser(
    private val result: ParsedTask? = null,
    private val shouldThrow: Boolean = false,
) : TaskParser(LLMService(ModelManager())) {

    override suspend fun parse(rawText: String): ParsedTask {
        if (shouldThrow) throw RuntimeException("LLM failed")
        return result ?: throw RuntimeException("No result configured")
    }
}
