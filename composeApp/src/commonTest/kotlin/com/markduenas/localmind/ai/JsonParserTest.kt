package com.markduenas.localmind.ai

import com.markduenas.localmind.domain.model.ParsedCapture
import com.markduenas.localmind.domain.model.ParsedTask
import com.markduenas.localmind.domain.model.Priority
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class JsonParserTest {

    /** Helper to unwrap a TaskCapture from the result. */
    private fun parseAsTask(json: String, original: String): ParsedTask {
        val capture = JsonParser.parse(json, original)
        assertIs<ParsedCapture.TaskCapture>(capture)
        return capture.task
    }

    @Test
    fun parsesValidJsonResponse() {
        val json = """{"title":"Call mom","due_date":"2026-02-17","due_time":"14:00","priority":"high","tags":["family"],"confidence":0.95}"""
        val result = parseAsTask(json, "call mom tomorrow")

        assertEquals("Call mom", result.title)
        assertEquals(LocalDate(2026, 2, 17), result.dueDate)
        assertEquals(LocalTime(14, 0), result.dueTime)
        assertEquals(Priority.HIGH, result.priority)
        assertEquals(listOf("family"), result.tags)
        assertEquals(0.95f, result.confidence)
        assertEquals("call mom tomorrow", result.originalText)
    }

    @Test
    fun parsesJsonWithMarkdownFences() {
        val json = """
            ```json
            {"title":"Fix bug","due_date":null,"due_time":null,"priority":"medium","tags":["work"],"confidence":0.9}
            ```
        """.trimIndent()
        val result = parseAsTask(json, "fix bug #work")

        assertEquals("Fix bug", result.title)
        assertNull(result.dueDate)
        assertEquals(Priority.MEDIUM, result.priority)
        assertEquals(listOf("work"), result.tags)
    }

    @Test
    fun parsesJsonWithSurroundingText() {
        val json = """Here is the parsed task: {"title":"Buy groceries","due_date":null,"due_time":null,"priority":"low","tags":[],"confidence":0.8} Hope that helps!"""
        val result = parseAsTask(json, "buy groceries")

        assertEquals("Buy groceries", result.title)
        assertEquals(Priority.LOW, result.priority)
    }

    @Test
    fun handlesNullDateAndTime() {
        val json = """{"title":"Do laundry","due_date":null,"due_time":null,"priority":"medium","tags":[],"confidence":0.85}"""
        val result = parseAsTask(json, "do laundry")

        assertNull(result.dueDate)
        assertNull(result.dueTime)
    }

    @Test
    fun parses12HourDueTimeFromJson() {
        val json = """{"title":"Call mom","due_date":"2026-03-20","due_time":"6pm","priority":"medium","tags":[],"confidence":0.9}"""
        val result = parseAsTask(json, "call mom tomorrow")

        assertEquals(LocalTime(18, 0), result.dueTime)
    }

    @Test
    fun fallsBackToOriginalTextTimeWhenJsonTimeMissing() {
        val json = """{"title":"Call mom","due_date":"2026-03-20","due_time":null,"priority":"medium","tags":[],"confidence":0.9}"""
        val result = parseAsTask(json, "call mom at 6:30pm")

        assertEquals(LocalTime(18, 30), result.dueTime)
    }

    @Test
    fun fallsBackToOriginalTextDateAndTimeWhenJsonIsNull() {
        val json = """
            ```json
            {"type":"task","title":"","body":null,"due_date":null,"due_time":null,"priority":"medium","tags":[],"confidence":0.0}
            ```<end_of_turn>
        """.trimIndent()
        val result = parseAsTask(json, "Pick up rocks tomorrow at the nursery at 9.30 in the morning")
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        assertEquals(today.plus(1, DateTimeUnit.DAY), result.dueDate)
        assertEquals(LocalTime(9, 30), result.dueTime)
    }

    @Test
    fun handlesMissingOptionalFields() {
        val json = """{"title":"Quick task"}"""
        val result = parseAsTask(json, "quick task")

        assertEquals("Quick task", result.title)
        assertNull(result.dueDate)
        assertNull(result.dueTime)
        assertEquals(Priority.MEDIUM, result.priority)
        assertEquals(emptyList(), result.tags)
    }

    @Test
    fun throwsOnNoJsonFound() {
        assertFailsWith<LLMException> {
            JsonParser.parse("no json here at all", "raw")
        }
    }

    @Test
    fun extractsJsonFromBrackets() {
        val raw = JsonParser.extractJson("""some text {"key":"val"} more text""")
        assertEquals("""{"key":"val"}""", raw)
    }

    @Test
    fun handlesInvalidDateGracefully() {
        val json = """{"title":"Test","due_date":"not-a-date","due_time":null,"priority":"medium","tags":[],"confidence":0.8}"""
        val result = parseAsTask(json, "test")

        assertEquals("Test", result.title)
        assertNull(result.dueDate)
    }

    @Test
    fun parsesNoteTypeJson() {
        val json = """{"type":"note","title":"Great pasta","body":"Great pasta at the Italian place","due_date":null,"due_time":null,"priority":"medium","tags":["food"],"confidence":0.9}"""
        val capture = JsonParser.parse(json, "great pasta at the Italian place")
        assertIs<ParsedCapture.NoteCapture>(capture)
        assertEquals("Great pasta", capture.note.title)
        assertEquals("Great pasta at the Italian place", capture.note.body)
        assertEquals(listOf("food"), capture.note.tags)
    }

    @Test
    fun defaultsToTaskWhenTypeMissing() {
        val json = """{"title":"Buy groceries","due_date":null,"priority":"medium","tags":[],"confidence":0.85}"""
        val capture = JsonParser.parse(json, "buy groceries")
        assertIs<ParsedCapture.TaskCapture>(capture)
    }

    @Test
    fun parsesPriorityObjectWithName() {
        val json = """{"title":"Get milk","due_date":"2026-03-12","priority":{"name":"low"},"tags":[],"confidence":0.7}"""
        val result = parseAsTask(json, "get milk on thursday")
        assertEquals(Priority.LOW, result.priority)
    }

    @Test
    fun parsesTagsWhenModelReturnsSingleString() {
        val json = """{"title":"Fix bug","priority":"high","tags":"work"}"""
        val result = parseAsTask(json, "fix bug #work")
        assertEquals(listOf("work"), result.tags)
    }

    @Test
    fun parsesNestedNoteObject() {
        val json = """{"note":{"title":"Great sushi","body":"Great sushi on Main St"},"tags":["food"]}"""
        val capture = JsonParser.parse(json, "great sushi on Main St")
        assertIs<ParsedCapture.NoteCapture>(capture)
        assertEquals("Great sushi", capture.note.title)
        assertEquals("Great sushi on Main St", capture.note.body)
    }

    @Test
    fun recoversMalformedMergedResponseFromTinyModel() {
        val json = """
            ```json
            {"type":{"type":"task"},
            {"note":{"title":"..."},body:null}
            ```<end_of_turn>
        """.trimIndent()

        val result = parseAsTask(json, "buy chicken feed today")
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        assertEquals("buy chicken feed today", result.title)
        assertEquals(today, result.dueDate)
        assertNull(result.dueTime)
        assertEquals(Priority.MEDIUM, result.priority)
    }

    @Test
    fun recoversNoteWithUnquotedBodyKey() {
        val json = """{"type":"note","note":{"title":"Trip ideas"},body:"Visit Boise in April","tags":["travel"]}"""
        val capture = JsonParser.parse(json, "visit boise in april")
        assertIs<ParsedCapture.NoteCapture>(capture)
        assertEquals("Trip ideas", capture.note.title)
        assertEquals("Visit Boise in April", capture.note.body)
        assertEquals(listOf("travel"), capture.note.tags)
    }

    @Test
    fun handlesRetryResponseWithInvalidTemplateObjectOnIosRegexEngine() {
        val json = """
            ```json
            {"type":"task","title":"","body":null,"due_date":null,"due_time":null,"priority":"medium","tags":[],"confidence":0.0}
            ```
            <end_of_turn>
            --- retry ---
            ```json
            {
              "type": "task" or "note" (default "task") {
                "title", "day_date", null, ...less complete than the original but not specified yet in this response
              }
            }
            ```
            <end of turn>
        """.trimIndent()

        val result = parseAsTask(json, "Pick up some milk at Winco tomorrow at 9.30 in the morning.")
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        assertEquals(today.plus(1, DateTimeUnit.DAY), result.dueDate)
        assertEquals(LocalTime(9, 30), result.dueTime)
        assertEquals(Priority.MEDIUM, result.priority)
    }
}
