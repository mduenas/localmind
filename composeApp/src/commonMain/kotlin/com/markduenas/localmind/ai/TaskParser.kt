package com.markduenas.localmind.ai

import com.markduenas.localmind.domain.model.ParsedTask
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class TaskParser(
    private val llmService: LLMService
) {
    suspend fun parse(rawText: String): ParsedTask {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val userPrompt = Prompts.buildUserPrompt(rawText, today)
        val response = llmService.generateCompletion(
            systemPrompt = Prompts.SYSTEM_PROMPT,
            userPrompt = userPrompt
        )
        return JsonParser.parse(response, rawText)
    }
}
