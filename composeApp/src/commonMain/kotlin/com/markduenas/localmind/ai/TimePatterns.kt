package com.markduenas.localmind.ai

import kotlinx.datetime.LocalTime

internal object TimePatterns {

    /**
     * Normalises whitespace (including non-breaking spaces from Android voice
     * input) and strips periods from am/pm so all patterns see clean input.
     */
    private fun normalise(text: String): String =
        text.replace('\u00A0', ' ')   // non-breaking space
            .replace('\u202F', ' ')   // narrow no-break space
            .replace(Regex("(?<=[ap])\\.(?=m\\.?)", RegexOption.IGNORE_CASE), "") // a.m. → am
            .replace(Regex("(?<=[ap]m)\\.", RegexOption.IGNORE_CASE), "")         // am. → am

    private val patterns = listOf(
        // "at 3pm", "at 3:30pm", "at 3 pm"
        Regex(
            "\\bat\\s+(\\d{1,2})(?:[:.](\\d{2}))?\\s*(am|pm)\\b",
            RegexOption.IGNORE_CASE
        ) to { m: MatchResult ->
            resolve12Hour(
                m.groupValues[1].toIntOrNull() ?: return@to null,
                m.groupValues[2].toIntOrNull() ?: 0,
                m.groupValues[3]
            )
        },

        // "at 15:00", "at 9:30"
        Regex("\\bat\\s+(\\d{1,2})[:.](\\d{2})\\b") to { m: MatchResult ->
            resolve24Hour(
                m.groupValues[1].toIntOrNull() ?: return@to null,
                m.groupValues[2].toIntOrNull() ?: 0
            )
        },

        // Standalone "3pm", "3:30pm", "3 pm"
        Regex(
            "\\b(\\d{1,2})(?:[:.](\\d{2}))?\\s*(am|pm)\\b",
            RegexOption.IGNORE_CASE
        ) to { m: MatchResult ->
            resolve12Hour(
                m.groupValues[1].toIntOrNull() ?: return@to null,
                m.groupValues[2].toIntOrNull() ?: 0,
                m.groupValues[3]
            )
        },
    )

    fun extract(text: String): LocalTime? {
        val cleaned = normalise(text)
        for ((pattern, resolver) in patterns) {
            val match = pattern.find(cleaned) ?: continue
            val time = resolver(match)
            if (time != null) return time
        }
        return null
    }

    fun removePatterns(text: String): String {
        var result = normalise(text)
        for ((pattern, _) in patterns) {
            result = pattern.replace(result, "")
        }
        return result
    }

    private fun resolve12Hour(hour: Int, minute: Int, amPm: String): LocalTime? {
        if (hour !in 1..12 || minute !in 0..59) return null
        val h = when {
            amPm.equals("am", ignoreCase = true) && hour == 12 -> 0
            amPm.equals("pm", ignoreCase = true) && hour != 12 -> hour + 12
            else -> hour
        }
        return LocalTime(h, minute)
    }

    private fun resolve24Hour(hour: Int, minute: Int): LocalTime? {
        if (hour !in 0..23 || minute !in 0..59) return null
        return LocalTime(hour, minute)
    }
}
