package com.markduenas.localmind.ai

import com.markduenas.localmind.domain.model.ParsedCapture
import kotlin.time.Clock
import kotlin.time.measureTimedValue
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class ParseOutput(val capture: ParsedCapture, val log: InferenceLog)
data class LLMParseException(
    val model: String,
    val systemPrompt: String,
    val userPrompt: String,
    val rawResponse: String,
    val durationMs: Long,
    val parseError: Throwable,
) : Exception("Failed to parse LLM response: ${parseError.message}", parseError)

open class TaskParser(
    private val llmService: LLMService?
) {
    open fun currentModelForLogging(): String = llmService?.currentModel ?: "unknown"

    open suspend fun parse(rawText: String): ParseOutput {
        val service = llmService ?: throw LLMException("LLM service unavailable")
        if (!service.isLoaded) service.initialize()

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val modelName = service.currentModel ?: "unknown"
        val systemPrompt = Prompts.systemPromptForModel(modelName)
        val userPrompt = Prompts.buildUserPrompt(rawText, today, modelName)
        val maxTokens = maxTokensForInput(rawText)

        val (response, duration) = measureTimedValue {
            service.generateCompletion(
                systemPrompt = systemPrompt,
                userPrompt = userPrompt,
                maxTokens = maxTokens,
            )
        }

        val capture = runCatching {
            JsonParser.parse(response, rawText)
        }.getOrElse {
            val retryPrompt = Prompts.buildRetryUserPrompt(rawText = rawText, todayDate = today, modelSlug = modelName)
            val (retryResponse, retryDuration) = measureTimedValue {
                service.generateCompletion(
                    systemPrompt = systemPrompt,
                    userPrompt = retryPrompt,
                    maxTokens = AIConfig.MAX_TOKENS_RETRY
                )
            }

            return try {
                val retryCapture = JsonParser.parse(retryResponse, rawText)
                val retryLog = InferenceLog(
                    model = modelName,
                    systemPrompt = systemPrompt,
                    userPrompt = retryPrompt,
                    rawResponse = retryResponse,
                    durationMs = duration.inWholeMilliseconds + retryDuration.inWholeMilliseconds,
                    method = "llm",
                )
                ParseOutput(retryCapture, retryLog)
            } catch (retryParseError: Exception) {
                val combinedResponse = buildString {
                    append(response)
                    append("\n--- retry ---\n")
                    append(retryResponse)
                }
                val combinedDuration = duration.inWholeMilliseconds + retryDuration.inWholeMilliseconds
                throw LLMParseException(
                    model = modelName,
                    systemPrompt = systemPrompt,
                    userPrompt = retryPrompt,
                    rawResponse = combinedResponse,
                    durationMs = combinedDuration,
                    parseError = retryParseError,
                )
            }
        }
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

    private fun maxTokensForInput(rawText: String): Int {
        val len = rawText.trim().length
        return when {
            len <= 80 -> AIConfig.MAX_TOKENS_SHORT_INPUT
            len <= 200 -> AIConfig.MAX_TOKENS_MEDIUM_INPUT
            else -> AIConfig.MAX_TOKENS_LONG_INPUT
        }
    }
}
