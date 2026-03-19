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
    companion object {
        private const val FAST_PATH_MAX_WORDS = 10
        private const val FAST_PATH_MAX_CHARS = 80
        private val FAST_PATH_COMPLEXITY_MARKERS = listOf(
            " if ",
            " when ",
            " because ",
            " after ",
            " before ",
            " unless ",
            " then ",
            ";",
            ":",
        )
    }

    suspend operator fun invoke(rawText: String): ParseResult {
        if (rawText.isBlank()) {
            return ParseResult.Error("Input text is empty")
        }

        if (isLLMEnabled() && isPremium() && shouldUseRuleFastPath(rawText)) {
            val parsed = ruleBasedParser.parse(rawText)
            val fastPathLog = InferenceLog(
                model = "rule-based",
                systemPrompt = "",
                userPrompt = rawText,
                rawResponse = null,
                durationMs = 0,
                method = "rule",
            )
            return ParseResult.Success(parsed, inferenceLog = fastPathLog)
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

    private fun shouldUseRuleFastPath(rawText: String): Boolean {
        val trimmed = rawText.trim()
        if (trimmed.length > FAST_PATH_MAX_CHARS) return false
        if (trimmed.contains('\n')) return false

        val lower = " ${trimmed.lowercase()} "
        if (FAST_PATH_COMPLEXITY_MARKERS.any { lower.contains(it) }) return false

        val wordCount = trimmed.split(Regex("\\s+")).count { it.isNotBlank() }
        return wordCount in 1..FAST_PATH_MAX_WORDS
    }
}
