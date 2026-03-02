package com.markduenas.localmind.ai

import com.markduenas.localmind.domain.model.ParsedCapture
import com.markduenas.localmind.domain.model.ParsedNote
import com.markduenas.localmind.domain.model.ParsedTask
import com.markduenas.localmind.domain.model.Priority
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val lenientJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

@Serializable
internal data class CaptureJson(
    val type: String = "task",
    val title: String,
    val body: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("due_time") val dueTime: String? = null,
    val priority: String = "medium",
    val tags: List<String> = emptyList(),
    val confidence: Float = 0.85f
)

object JsonParser {

    /**
     * Extracts the first JSON object from an LLM response that may contain
     * surrounding text, markdown fences, or multiple lines.
     */
    fun extractJson(raw: String): String {
        // Strip markdown code fences if present
        val stripped = raw
            .replace(Regex("```json\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("```\\s*"), "")
            .trim()

        // Find first { ... } block
        val start = stripped.indexOf('{')
        val end = stripped.lastIndexOf('}')
        if (start == -1 || end == -1 || end <= start) {
            throw LLMException("No JSON object found in LLM response")
        }
        return stripped.substring(start, end + 1)
    }

    fun parse(jsonString: String, originalText: String): ParsedCapture {
        val extracted = extractJson(jsonString)
        val captureJson = lenientJson.decodeFromString<CaptureJson>(extracted)
        return captureJson.toDomain(originalText)
    }
}

private fun CaptureJson.toDomain(originalText: String): ParsedCapture {
    return if (type.lowercase() == "note") {
        ParsedCapture.NoteCapture(
            ParsedNote(
                title = title,
                body = body ?: originalText,
                tags = tags,
                originalText = originalText,
                confidence = confidence
            )
        )
    } else {
        ParsedCapture.TaskCapture(
            ParsedTask(
                title = title,
                dueDate = dueDate?.let { parseDateSafe(it) },
                dueTime = dueTime?.let { parseTimeSafe(it) },
                priority = parsePriority(priority),
                tags = tags,
                originalText = originalText,
                confidence = confidence,
                suggestedEdits = null
            )
        )
    }
}

private fun parseDateSafe(value: String): LocalDate? {
    return try {
        LocalDate.parse(value)
    } catch (_: Exception) {
        null
    }
}

private fun parseTimeSafe(value: String): LocalTime? {
    return try {
        LocalTime.parse(value)
    } catch (_: Exception) {
        null
    }
}

private fun parsePriority(value: String): Priority {
    return when (value.lowercase()) {
        "high" -> Priority.HIGH
        "low" -> Priority.LOW
        else -> Priority.MEDIUM
    }
}
