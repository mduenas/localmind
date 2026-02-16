package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.ai.RuleBasedParser
import com.markduenas.localmind.ai.TaskParser
import com.markduenas.localmind.domain.model.ParseResult

class ParseCaptureUseCase(
    private val taskParser: TaskParser,
    private val ruleBasedParser: RuleBasedParser,
    private val isLLMEnabled: () -> Boolean = { false }
) {
    suspend operator fun invoke(rawText: String): ParseResult {
        if (rawText.isBlank()) {
            return ParseResult.Error("Input text is empty")
        }

        return if (isLLMEnabled()) {
            try {
                val parsed = taskParser.parse(rawText)
                ParseResult.Success(parsed)
            } catch (e: Exception) {
                // Fallback to rule-based parser
                val parsed = ruleBasedParser.parse(rawText)
                ParseResult.Fallback(parsed, reason = e.message)
            }
        } else {
            val parsed = ruleBasedParser.parse(rawText)
            ParseResult.Success(parsed)
        }
    }
}
