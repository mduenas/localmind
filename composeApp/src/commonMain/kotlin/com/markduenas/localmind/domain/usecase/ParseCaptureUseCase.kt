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
        val immediate = parseImmediate(rawText)
        if (immediate is ParseResult.Error) return immediate

        val enhanced = parseEnhancement(rawText)
        return enhanced ?: immediate
    }

    fun parseImmediate(rawText: String): ParseResult {
        if (rawText.isBlank()) {
            return ParseResult.Error("Input text is empty")
        }

        val parsed = ruleBasedParser.parse(rawText)
        val immediateLog = InferenceLog(
            model = "rule-based",
            systemPrompt = "",
            userPrompt = rawText,
            rawResponse = null,
            durationMs = 0,
            method = "rule-immediate",
        )
        return ParseResult.Success(parsed, inferenceLog = immediateLog)
    }

    suspend fun parseEnhancement(rawText: String): ParseResult? {
        if (rawText.isBlank()) return ParseResult.Error("Input text is empty")
        if (!isLLMEnabled() || !isPremium()) return null
        if (shouldSkipEnhancement(rawText)) return null

        return try {
            val output = taskParser.parse(rawText)
            ParseResult.Success(output.capture, inferenceLog = output.log.copy(method = "ai-enhance"))
        } catch (e: LLMParseException) {
            val failLog = InferenceLog(
                model = e.model,
                systemPrompt = e.systemPrompt,
                userPrompt = e.userPrompt,
                rawResponse = e.rawResponse,
                durationMs = e.durationMs,
                method = "ai-enhance-fallback",
                error = e.parseError.message,
            )
            val parsed = ruleBasedParser.parse(rawText)
            ParseResult.Fallback(parsed, reason = e.parseError.message, inferenceLog = failLog)
        } catch (e: Exception) {
            val failLog = InferenceLog(
                model = taskParser.currentModelForLogging(),
                systemPrompt = "",
                userPrompt = rawText,
                rawResponse = null,
                durationMs = 0,
                method = "ai-enhance-fallback",
                error = e.message,
            )
            val parsed = ruleBasedParser.parse(rawText)
            ParseResult.Fallback(parsed, reason = e.message, inferenceLog = failLog)
        }
    }

    private fun shouldSkipEnhancement(rawText: String): Boolean {
        val trimmed = rawText.trim()
        if (trimmed.length > FAST_PATH_MAX_CHARS) return false
        if (trimmed.contains('\n')) return false

        val lower = " ${trimmed.lowercase()} "
        if (FAST_PATH_COMPLEXITY_MARKERS.any { lower.contains(it) }) return false

        val wordCount = trimmed.split(Regex("\\s+")).count { it.isNotBlank() }
        return wordCount in 1..FAST_PATH_MAX_WORDS
    }
}
