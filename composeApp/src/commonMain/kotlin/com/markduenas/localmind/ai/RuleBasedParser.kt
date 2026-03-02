package com.markduenas.localmind.ai

import com.markduenas.localmind.domain.model.ParsedCapture
import com.markduenas.localmind.domain.model.ParsedNote
import com.markduenas.localmind.domain.model.ParsedTask
import com.markduenas.localmind.domain.model.Priority
import kotlinx.datetime.LocalDate

class RuleBasedParser {

    companion object {
        private val HASHTAG_PATTERN = Regex("#(\\w+)")
        private const val CONFIDENCE = 0.7f

        private val ACTION_VERBS = setOf(
            "buy", "call", "fix", "send", "schedule", "meet", "finish", "submit",
            "clean", "pick", "drop", "make", "write", "email", "text", "pay",
            "book", "cancel", "return", "order", "check", "review", "update",
            "complete", "prepare", "remind", "setup", "set up", "install",
            "create", "build", "organize", "attend", "visit", "deliver"
        )
    }

    fun parse(rawText: String): ParsedCapture {
        val dueDate = extractDate(rawText)
        val dueTime = TimePatterns.extract(rawText)
        val priority = extractPriority(rawText)
        val tags = extractTags(rawText)
        val title = buildTitle(rawText, dueDate, tags)

        return if (isLikelyNote(rawText, dueDate, dueTime != null, priority)) {
            ParsedCapture.NoteCapture(
                ParsedNote(
                    title = title,
                    body = rawText,
                    tags = tags,
                    originalText = rawText,
                    confidence = CONFIDENCE
                )
            )
        } else {
            ParsedCapture.TaskCapture(
                ParsedTask(
                    title = title,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    priority = priority,
                    tags = tags,
                    originalText = rawText,
                    confidence = CONFIDENCE,
                    suggestedEdits = null
                )
            )
        }
    }

    private fun isLikelyNote(
        rawText: String,
        dueDate: LocalDate?,
        hasTime: Boolean,
        priority: Priority
    ): Boolean {
        if (dueDate != null || hasTime) return false
        if (priority != Priority.MEDIUM) return false
        val lowerText = rawText.lowercase()
        val hasActionVerb = ACTION_VERBS.any { verb ->
            lowerText.contains(Regex("\\b$verb\\b"))
        }
        return !hasActionVerb
    }

    private fun extractDate(text: String): LocalDate? {
        for ((pattern, resolver) in DatePatterns.patterns) {
            val match = pattern.find(text) ?: continue
            val date = resolver(match)
            if (date != null) return date
        }
        return null
    }

    private fun extractPriority(text: String): Priority = PriorityPatterns.extract(text)

    private fun extractTags(text: String): List<String> {
        return HASHTAG_PATTERN.findAll(text)
            .map { it.groupValues[1] }
            .toList()
    }

    /**
     * Builds a clean title by stripping date phrases, hashtags, and priority keywords,
     * then trimming leftover whitespace/punctuation.
     */
    private fun buildTitle(rawText: String, dueDate: LocalDate?, tags: List<String>): String {
        var title = rawText

        // Remove hashtags
        title = HASHTAG_PATTERN.replace(title, "")

        // Remove matched date phrase if a date was found
        if (dueDate != null) {
            for ((pattern, _) in DatePatterns.patterns) {
                title = pattern.replace(title, "")
            }
        }

        // Remove time phrases
        TimePatterns.removePatterns(title).let { title = it }

        // Remove priority keywords
        title = PriorityPatterns.removePatterns(title)

        // Clean up whitespace and trailing punctuation
        title = title
            .replace(Regex("\\s{2,}"), " ")
            .trim()
            .trimEnd(',', '.', '-', '–', '—')
            .trim()

        return title.replaceFirstChar { it.uppercaseChar() }
    }
}
