package com.markduenas.localmind.ai

import com.markduenas.localmind.domain.model.ParsedCapture
import com.markduenas.localmind.domain.model.ParsedTask
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RuleBasedParserTest {

    private val parser = RuleBasedParser()
    private fun today() = Clock.System.todayIn(TimeZone.currentSystemDefault())

    /** Helper to unwrap a TaskCapture from the ParsedCapture result. */
    private fun parseAsTask(text: String): ParsedTask {
        val capture = parser.parse(text)
        assertIs<ParsedCapture.TaskCapture>(capture)
        return capture.task
    }

    // --- Date extraction ---

    @Test
    fun parsesTomorrowCorrectly() {
        val result = parseAsTask("call mom tomorrow")
        assertEquals(today().plus(1, DateTimeUnit.DAY), result.dueDate)
    }

    @Test
    fun parsesTodayCorrectly() {
        val result = parseAsTask("finish report today")
        assertEquals(today(), result.dueDate)
    }

    @Test
    fun parsesInNDays() {
        val result = parseAsTask("review PR in 3 days")
        assertEquals(today().plus(3, DateTimeUnit.DAY), result.dueDate)
    }

    @Test
    fun parsesInNWeeks() {
        val result = parseAsTask("plan trip in 2 weeks")
        assertEquals(today().plus(14, DateTimeUnit.DAY), result.dueDate)
    }

    @Test
    fun parsesNextWeek() {
        val result = parseAsTask("team sync next week")
        assertNotNull(result.dueDate)
        assertEquals(DayOfWeek.MONDAY, result.dueDate!!.dayOfWeek)
    }

    @Test
    fun parsesMonthDayFormat() {
        val result = parseAsTask("dentist March 15")
        assertNotNull(result.dueDate)
        assertEquals(3, result.dueDate!!.monthNumber)
        assertEquals(15, result.dueDate!!.dayOfMonth)
    }

    @Test
    fun parsesMonthAbbreviationWithOrdinal() {
        val result = parseAsTask("meeting on Jan 3rd")
        assertNotNull(result.dueDate)
        assertEquals(1, result.dueDate!!.monthNumber)
        assertEquals(3, result.dueDate!!.dayOfMonth)
    }

    @Test
    fun parsesNoDateReturnsNull() {
        // "buy groceries" has an action verb, so it should still be a task
        val result = parseAsTask("buy groceries")
        assertNull(result.dueDate)
    }

    @Test
    fun parsesIsoDate() {
        val result = parseAsTask("deadline 2026-06-01")
        assertNotNull(result.dueDate)
        assertEquals(2026, result.dueDate!!.year)
        assertEquals(6, result.dueDate!!.monthNumber)
        assertEquals(1, result.dueDate!!.dayOfMonth)
    }

    @Test
    fun parsesThisWeekend() {
        val result = parseAsTask("clean house this weekend")
        assertNotNull(result.dueDate)
        assertEquals(DayOfWeek.SATURDAY, result.dueDate!!.dayOfWeek)
    }

    @Test
    fun parsesDayAfterTomorrow() {
        val result = parseAsTask("submit form day after tomorrow")
        assertEquals(today().plus(2, DateTimeUnit.DAY), result.dueDate)
    }

    // --- Time extraction ---

    @Test
    fun parsesTimeAt3pm() {
        val result = parseAsTask("meeting at 3pm tomorrow")
        assertEquals(LocalTime(15, 0), result.dueTime)
    }

    @Test
    fun parsesTimeAt930am() {
        val result = parseAsTask("standup at 9:30am")
        assertEquals(LocalTime(9, 30), result.dueTime)
    }

    @Test
    fun parsesNoTimeReturnsNull() {
        val result = parseAsTask("buy groceries tomorrow")
        assertNull(result.dueTime)
    }

    // --- Priority extraction ---

    @Test
    fun detectsHighPriorityUrgent() {
        val result = parseAsTask("URGENT fix server")
        assertEquals(Priority.HIGH, result.priority)
    }

    @Test
    fun detectsHighPriorityAsap() {
        val result = parseAsTask("deploy hotfix asap")
        assertEquals(Priority.HIGH, result.priority)
    }

    @Test
    fun detectsLowPriorityNoRush() {
        val result = parseAsTask("update docs no rush")
        assertEquals(Priority.LOW, result.priority)
    }

    @Test
    fun detectsLowPrioritySomeday() {
        // "learn" is not in ACTION_VERBS and "someday" triggers LOW priority,
        // but LOW priority prevents note classification, so this stays as task
        val result = parseAsTask("learn rust someday")
        assertEquals(Priority.LOW, result.priority)
    }

    @Test
    fun defaultsMediumPriority() {
        val result = parseAsTask("buy groceries")
        assertEquals(Priority.MEDIUM, result.priority)
    }

    // --- Tag extraction ---

    @Test
    fun extractsHashtagsAsTags() {
        val result = parseAsTask("fix bug #work #urgent")
        assertEquals(listOf("work", "urgent"), result.tags)
    }

    @Test
    fun noHashtagsReturnsEmptyList() {
        val result = parseAsTask("buy milk")
        assertEquals(emptyList(), result.tags)
    }

    @Test
    fun extractsMultipleTags() {
        val result = parseAsTask("#home clean the #kitchen and #bathroom")
        assertEquals(listOf("home", "kitchen", "bathroom"), result.tags)
    }

    // --- Title building ---

    @Test
    fun titleStripsDatePhrase() {
        val result = parseAsTask("call mom tomorrow")
        assertEquals("Call mom", result.title)
    }

    @Test
    fun titleStripsHashtags() {
        val result = parseAsTask("fix bug #work")
        assertEquals("Fix bug", result.title)
    }

    @Test
    fun titleStripsPriorityKeywords() {
        val result = parseAsTask("urgent fix the server")
        assertEquals("Fix the server", result.title)
    }

    @Test
    fun titleCleansComplexInput() {
        val result = parseAsTask("urgent call dentist tomorrow at 3pm #health")
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
        val result = parseAsTask(raw)
        assertEquals(raw, result.originalText)
    }

    @Test
    fun confidenceIs07() {
        val result = parseAsTask("buy something")
        assertEquals(0.7f, result.confidence)
    }

    // --- Note classification ---

    @Test
    fun classifiesObservationAsNote() {
        val capture = parser.parse("great pasta at the Italian place")
        assertIs<ParsedCapture.NoteCapture>(capture)
        assertEquals("great pasta at the Italian place", capture.note.body)
    }

    @Test
    fun classifiesActionVerbAsTask() {
        val capture = parser.parse("buy groceries")
        assertIs<ParsedCapture.TaskCapture>(capture)
    }

    @Test
    fun classifiesInputWithDateAsTask() {
        val capture = parser.parse("something interesting tomorrow")
        assertIs<ParsedCapture.TaskCapture>(capture)
    }
}
