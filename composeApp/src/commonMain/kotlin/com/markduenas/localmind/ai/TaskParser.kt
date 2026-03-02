package com.markduenas.localmind.ai

import com.markduenas.localmind.domain.model.ParsedCapture
import kotlin.time.Clock
import kotlin.time.measureTimedValue
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class ParseOutput(val capture: ParsedCapture, val log: InferenceLog)

open class TaskParser(
    private val llmService: LLMService?
) {
    open suspend fun parse(rawText: String): ParseOutput {
        if (!llmService!!.isLoaded) llmService!!.initialize()

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val systemPrompt = Prompts.SYSTEM_PROMPT
        val userPrompt = Prompts.buildUserPrompt(rawText, today)
        val modelName = llmService!!.currentModel ?: "unknown"

        val (response, duration) = measureTimedValue {
            llmService!!.generateCompletion(
                systemPrompt = systemPrompt,
                userPrompt = userPrompt
            )
        }

        val capture = JsonParser.parse(response, rawText)
        val log = InferenceLog(
            model = modelName,
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            rawResponse = response,
            durationMs = duration.inWholeMilliseconds,
            method = "llm",
        )
        return ParseOutput(capture, log)
    }
}
