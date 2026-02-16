package com.markduenas.localmind.ai

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

internal object DatePatterns {

    private val timeZone get() = TimeZone.currentSystemDefault()
    private fun today(): LocalDate = Clock.System.todayIn(timeZone)

    /**
     * Ordered list of (regex, resolver) pairs. First match wins.
     */
    val patterns: List<Pair<Regex, (MatchResult) -> LocalDate?>> = listOf(
        // "today"
        Regex("\\btoday\\b", RegexOption.IGNORE_CASE) to { _ -> today() },

        // "tonight"
        Regex("\\btonight\\b", RegexOption.IGNORE_CASE) to { _ -> today() },

        // "day after tomorrow" — must come before "tomorrow"
        Regex("\\bday after tomorrow\\b", RegexOption.IGNORE_CASE) to { _ ->
            today().plus(2, DateTimeUnit.DAY)
        },

        // "tomorrow"
        Regex("\\btomorrow\\b", RegexOption.IGNORE_CASE) to { _ ->
            today().plus(1, DateTimeUnit.DAY)
        },

        // "in N days/weeks"
        Regex("\\bin\\s+(\\d+)\\s+(day|days|week|weeks)\\b", RegexOption.IGNORE_CASE) to { m ->
            val n = m.groupValues[1].toIntOrNull() ?: return@to null
            val unit = m.groupValues[2].lowercase()
            when {
                unit.startsWith("day") -> today().plus(n, DateTimeUnit.DAY)
                unit.startsWith("week") -> today().plus(n * 7, DateTimeUnit.DAY)
                else -> null
            }
        },

        // "next Monday/Tuesday/..."
        Regex(
            "\\bnext\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b",
            RegexOption.IGNORE_CASE
        ) to { m ->
            resolveNextDayOfWeek(parseDayOfWeek(m.groupValues[1]))
        },

        // Bare day-of-week: "Monday", "tuesday" etc. — resolves to the upcoming occurrence
        Regex(
            "\\b(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b",
            RegexOption.IGNORE_CASE
        ) to { m ->
            resolveUpcomingDayOfWeek(parseDayOfWeek(m.groupValues[1]))
        },

        // "next week" — next Monday
        Regex("\\bnext\\s+week\\b", RegexOption.IGNORE_CASE) to { _ ->
            resolveNextDayOfWeek(DayOfWeek.MONDAY)
        },

        // "this weekend" — upcoming Saturday
        Regex("\\bthis\\s+weekend\\b", RegexOption.IGNORE_CASE) to { _ ->
            resolveUpcomingDayOfWeek(DayOfWeek.SATURDAY)
        },

        // Month Day: "March 15", "Jan 3", "feb 28th"
        Regex(
            "\\b(jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:t(?:ember)?)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?)\\s+(\\d{1,2})(?:st|nd|rd|th)?\\b",
            RegexOption.IGNORE_CASE
        ) to { m ->
            resolveMonthDay(m.groupValues[1], m.groupValues[2].toIntOrNull() ?: return@to null)
        },

        // Day Month: "15 March", "3rd Jan"
        Regex(
            "\\b(\\d{1,2})(?:st|nd|rd|th)?\\s+(jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:t(?:ember)?)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?)\\b",
            RegexOption.IGNORE_CASE
        ) to { m ->
            resolveMonthDay(m.groupValues[2], m.groupValues[1].toIntOrNull() ?: return@to null)
        },

        // ISO-ish: "2026-03-15" or "2026/03/15"
        Regex("\\b(\\d{4})[/-](\\d{1,2})[/-](\\d{1,2})\\b") to { m ->
            try {
                LocalDate(
                    m.groupValues[1].toInt(),
                    m.groupValues[2].toInt(),
                    m.groupValues[3].toInt()
                )
            } catch (_: Exception) {
                null
            }
        },

        // US-style: "3/15" or "03/15" (month/day, current or next year)
        Regex("\\b(\\d{1,2})/(\\d{1,2})\\b") to { m ->
            val month = m.groupValues[1].toIntOrNull() ?: return@to null
            val day = m.groupValues[2].toIntOrNull() ?: return@to null
            if (month !in 1..12 || day !in 1..31) return@to null
            resolveMonthDayNumeric(month, day)
        },
    )

    // --- Helpers ---

    private fun parseDayOfWeek(name: String): DayOfWeek = when (name.lowercase()) {
        "monday" -> DayOfWeek.MONDAY
        "tuesday" -> DayOfWeek.TUESDAY
        "wednesday" -> DayOfWeek.WEDNESDAY
        "thursday" -> DayOfWeek.THURSDAY
        "friday" -> DayOfWeek.FRIDAY
        "saturday" -> DayOfWeek.SATURDAY
        "sunday" -> DayOfWeek.SUNDAY
        else -> DayOfWeek.MONDAY
    }

    /**
     * "next Monday" — skips to the Monday of next week (always 7+ days out).
     */
    private fun resolveNextDayOfWeek(target: DayOfWeek): LocalDate {
        val t = today()
        val todayOrd = t.dayOfWeek.ordinal
        val targetOrd = target.ordinal
        val daysAhead = (targetOrd - todayOrd + 7) % 7
        val offset = if (daysAhead == 0) 7 else daysAhead
        return t.plus(offset + 7, DateTimeUnit.DAY).let {
            // Actually: skip to next week's occurrence
            t.plus(((targetOrd - todayOrd + 7) % 7).let { d -> if (d == 0) 7 else d + 7 }, DateTimeUnit.DAY)
        }
    }

    /**
     * Bare day-of-week — the next upcoming occurrence (1-7 days out).
     */
    private fun resolveUpcomingDayOfWeek(target: DayOfWeek): LocalDate {
        val t = today()
        val daysAhead = (target.ordinal - t.dayOfWeek.ordinal + 7) % 7
        val offset = if (daysAhead == 0) 7 else daysAhead
        return t.plus(offset, DateTimeUnit.DAY)
    }

    private fun parseMonth(name: String): Int = when (name.lowercase().take(3)) {
        "jan" -> 1; "feb" -> 2; "mar" -> 3; "apr" -> 4
        "may" -> 5; "jun" -> 6; "jul" -> 7; "aug" -> 8
        "sep" -> 9; "oct" -> 10; "nov" -> 11; "dec" -> 12
        else -> 0
    }

    private fun resolveMonthDay(monthName: String, day: Int): LocalDate? {
        val month = parseMonth(monthName)
        if (month == 0) return null
        return resolveMonthDayNumeric(month, day)
    }

    /**
     * Resolves month/day to the nearest future date (this year or next).
     */
    private fun resolveMonthDayNumeric(month: Int, day: Int): LocalDate? {
        val t = today()
        return try {
            val thisYear = LocalDate(t.year, month, day)
            if (thisYear >= t) thisYear else LocalDate(t.year + 1, month, day)
        } catch (_: Exception) {
            null
        }
    }
}
