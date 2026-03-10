package com.markduenas.localmind.ai

import com.markduenas.localmind.domain.model.ParsedCapture
import com.markduenas.localmind.domain.model.ParsedTask
import com.markduenas.localmind.domain.model.Priority
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
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
}
