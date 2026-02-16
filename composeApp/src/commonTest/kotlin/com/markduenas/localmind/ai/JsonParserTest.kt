package com.markduenas.localmind.ai

import com.markduenas.localmind.domain.model.Priority
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JsonParserTest {

    @Test
    fun parsesValidJsonResponse() {
        val json = """{"title":"Call mom","due_date":"2026-02-17","due_time":"14:00","priority":"high","tags":["family"],"confidence":0.95}"""
        val result = JsonParser.parse(json, "call mom tomorrow")

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
        val result = JsonParser.parse(json, "fix bug #work")

        assertEquals("Fix bug", result.title)
        assertNull(result.dueDate)
        assertEquals(Priority.MEDIUM, result.priority)
        assertEquals(listOf("work"), result.tags)
    }

    @Test
    fun parsesJsonWithSurroundingText() {
        val json = """Here is the parsed task: {"title":"Buy groceries","due_date":null,"due_time":null,"priority":"low","tags":[],"confidence":0.8} Hope that helps!"""
        val result = JsonParser.parse(json, "buy groceries")

        assertEquals("Buy groceries", result.title)
        assertEquals(Priority.LOW, result.priority)
    }

    @Test
    fun handlesNullDateAndTime() {
        val json = """{"title":"Do laundry","due_date":null,"due_time":null,"priority":"medium","tags":[],"confidence":0.85}"""
        val result = JsonParser.parse(json, "do laundry")

        assertNull(result.dueDate)
        assertNull(result.dueTime)
    }

    @Test
    fun handlesMissingOptionalFields() {
        val json = """{"title":"Quick task"}"""
        val result = JsonParser.parse(json, "quick task")

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
        val result = JsonParser.parse(json, "test")

        assertEquals("Test", result.title)
        assertNull(result.dueDate)
    }
}
