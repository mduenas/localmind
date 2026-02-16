package com.markduenas.localmind.ai

import com.markduenas.localmind.domain.model.Priority

internal object PriorityPatterns {

    private val highPatterns = listOf(
        "urgent",
        "asap",
        "critical",
        "important",
        "immediately",
        "high priority",
        "high-priority",
        "emergency",
        "right away",
        "top priority",
    )

    private val lowPatterns = listOf(
        "low priority",
        "low-priority",
        "whenever",
        "no rush",
        "someday",
        "eventually",
        "not urgent",
        "when you can",
        "if you get a chance",
    )

    private val allPatterns = highPatterns + lowPatterns

    fun extract(text: String): Priority {
        val lower = text.lowercase()
        return when {
            highPatterns.any { it in lower } -> Priority.HIGH
            lowPatterns.any { it in lower } -> Priority.LOW
            else -> Priority.MEDIUM
        }
    }

    fun removePatterns(text: String): String {
        var result = text
        for (keyword in allPatterns) {
            result = result.replace(Regex("\\b${Regex.escape(keyword)}\\b", RegexOption.IGNORE_CASE), "")
        }
        return result
    }
}
