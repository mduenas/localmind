package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.ai.InferenceLog
import com.markduenas.localmind.ai.LLMParseException
import com.markduenas.localmind.ai.RuleBasedParser
import com.markduenas.localmind.ai.TaskParser
import com.markduenas.localmind.domain.model.ParseResult

class ParseCaptureUseCase(
    private val taskParser: TaskParser,
    private val ruleBasedParser: RuleBasedParser,
    private val isLLMEnabled: () -> Boolean = { false },
    private val isPremium: () -> Boolean = { false },
) {
    suspend operator fun invoke(rawText: String): ParseResult {
        if (rawText.isBlank()) {
            return ParseResult.Error("Input text is empty")
        }

        return if (isLLMEnabled() && isPremium()) {
            try {
                val output = taskParser.parse(rawText)
                ParseResult.Success(output.capture, inferenceLog = output.log)
            } catch (e: LLMParseException) {
                val failLog = InferenceLog(
                    model = e.model,
                    systemPrompt = e.systemPrompt,
                    userPrompt = e.userPrompt,
                    rawResponse = e.rawResponse,
                    durationMs = e.durationMs,
                    method = "fallback",
                    error = e.parseError.message,
                )
                val parsed = ruleBasedParser.parse(rawText)
                ParseResult.Fallback(parsed, reason = e.parseError.message, inferenceLog = failLog)
            } catch (e: Exception) {
                // Build a partial log for the failed attempt
                val failLog = InferenceLog(
                    model = taskParser.currentModelForLogging(),
                    systemPrompt = "",
                    userPrompt = rawText,
                    rawResponse = null,
                    durationMs = 0,
                    method = "fallback",
                    error = e.message,
                )
                val parsed = ruleBasedParser.parse(rawText)
                ParseResult.Fallback(parsed, reason = e.message, inferenceLog = failLog)
            }
        } else {
            val parsed = ruleBasedParser.parse(rawText)
            ParseResult.Success(parsed)
        }
    }
}
