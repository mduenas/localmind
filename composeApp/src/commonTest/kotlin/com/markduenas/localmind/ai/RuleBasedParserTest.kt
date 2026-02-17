package com.markduenas.localmind.ai

import com.markduenas.localmind.domain.model.Priority
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RuleBasedParserTest {

    private val parser = RuleBasedParser()
    private fun today() = Clock.System.todayIn(TimeZone.currentSystemDefault())

    // --- Date extraction ---

    @Test
    fun parsesTomorrowCorrectly() {
        val result = parser.parse("call mom tomorrow")
        assertEquals(today().plus(1, DateTimeUnit.DAY), result.dueDate)
    }

    @Test
    fun parsesTodayCorrectly() {
        val result = parser.parse("finish report today")
        assertEquals(today(), result.dueDate)
    }

    @Test
    fun parsesInNDays() {
        val result = parser.parse("review PR in 3 days")
        assertEquals(today().plus(3, DateTimeUnit.DAY), result.dueDate)
    }

    @Test
    fun parsesInNWeeks() {
        val result = parser.parse("plan trip in 2 weeks")
        assertEquals(today().plus(14, DateTimeUnit.DAY), result.dueDate)
    }

    @Test
    fun parsesNextWeek() {
        val result = parser.parse("team sync next week")
        assertNotNull(result.dueDate)
        assertEquals(DayOfWeek.MONDAY, result.dueDate!!.dayOfWeek)
    }

    @Test
    fun parsesMonthDayFormat() {
        val result = parser.parse("dentist March 15")
        assertNotNull(result.dueDate)
        assertEquals(3, result.dueDate!!.monthNumber)
        assertEquals(15, result.dueDate!!.dayOfMonth)
    }

    @Test
    fun parsesMonthAbbreviationWithOrdinal() {
        val result = parser.parse("meeting on Jan 3rd")
        assertNotNull(result.dueDate)
        assertEquals(1, result.dueDate!!.monthNumber)
        assertEquals(3, result.dueDate!!.dayOfMonth)
    }

    @Test
    fun parsesNoDateReturnsNull() {
        val result = parser.parse("buy groceries")
        assertNull(result.dueDate)
    }

    @Test
    fun parsesIsoDate() {
        val result = parser.parse("deadline 2026-06-01")
        assertNotNull(result.dueDate)
        assertEquals(2026, result.dueDate!!.year)
        assertEquals(6, result.dueDate!!.monthNumber)
        assertEquals(1, result.dueDate!!.dayOfMonth)
    }

    @Test
    fun parsesThisWeekend() {
        val result = parser.parse("clean house this weekend")
        assertNotNull(result.dueDate)
        assertEquals(DayOfWeek.SATURDAY, result.dueDate!!.dayOfWeek)
    }

    @Test
    fun parsesDayAfterTomorrow() {
        val result = parser.parse("submit form day after tomorrow")
        assertEquals(today().plus(2, DateTimeUnit.DAY), result.dueDate)
    }

    // --- Time extraction ---

    @Test
    fun parsesTimeAt3pm() {
        val result = parser.parse("meeting at 3pm tomorrow")
        assertEquals(LocalTime(15, 0), result.dueTime)
    }

    @Test
    fun parsesTimeAt930am() {
        val result = parser.parse("standup at 9:30am")
        assertEquals(LocalTime(9, 30), result.dueTime)
    }

    @Test
    fun parsesNoTimeReturnsNull() {
        val result = parser.parse("buy groceries tomorrow")
        assertNull(result.dueTime)
    }

    // --- Priority extraction ---

    @Test
    fun detectsHighPriorityUrgent() {
        val result = parser.parse("URGENT fix server")
        assertEquals(Priority.HIGH, result.priority)
    }

    @Test
    fun detectsHighPriorityAsap() {
        val result = parser.parse("deploy hotfix asap")
        assertEquals(Priority.HIGH, result.priority)
    }

    @Test
    fun detectsLowPriorityNoRush() {
        val result = parser.parse("update docs no rush")
        assertEquals(Priority.LOW, result.priority)
    }

    @Test
    fun detectsLowPrioritySomeday() {
        val result = parser.parse("learn rust someday")
        assertEquals(Priority.LOW, result.priority)
    }

    @Test
    fun defaultsMediumPriority() {
        val result = parser.parse("buy groceries")
        assertEquals(Priority.MEDIUM, result.priority)
    }

    // --- Tag extraction ---

    @Test
    fun extractsHashtagsAsTags() {
        val result = parser.parse("fix bug #work #urgent")
        assertEquals(listOf("work", "urgent"), result.tags)
    }

    @Test
    fun noHashtagsReturnsEmptyList() {
        val result = parser.parse("buy milk")
        assertEquals(emptyList(), result.tags)
    }

    @Test
    fun extractsMultipleTags() {
        val result = parser.parse("#home clean the #kitchen and #bathroom")
        assertEquals(listOf("home", "kitchen", "bathroom"), result.tags)
    }

    // --- Title building ---

    @Test
    fun titleStripsDatePhrase() {
        val result = parser.parse("call mom tomorrow")
        assertEquals("Call mom", result.title)
    }

    @Test
    fun titleStripsHashtags() {
        val result = parser.parse("fix bug #work")
        assertEquals("Fix bug", result.title)
    }

    @Test
    fun titleStripsPriorityKeywords() {
        val result = parser.parse("urgent fix the server")
        assertEquals("Fix the server", result.title)
    }

    @Test
    fun titleCleansComplexInput() {
        val result = parser.parse("urgent call dentist tomorrow at 3pm #health")
        assertEquals("Call dentist", result.title)
        assertEquals(today().plus(1, DateTimeUnit.DAY), result.dueDate)
        assertEquals(LocalTime(15, 0), result.dueTime)
        assertEquals(Priority.HIGH, result.priority)
        assertEquals(listOf("health"), result.tags)
    }

    // --- Metadata ---

    @Test
    fun preservesOriginalText() {
        val raw = "buy milk tomorrow #grocery"
        val result = parser.parse(raw)
        assertEquals(raw, result.originalText)
    }

    @Test
    fun confidenceIs07() {
        val result = parser.parse("anything")
        assertEquals(0.7f, result.confidence)
    }
}
