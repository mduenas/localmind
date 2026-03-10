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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

private val lenientJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

@Serializable
internal data class CaptureJson(
    val type: String = "task",
    val title: String = "",
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
        val captureJson = decodeCaptureJson(extracted)
        return captureJson.toDomain(originalText)
    }

    private fun decodeCaptureJson(rawJson: String): CaptureJson {
        var lastError: Throwable? = null
        val candidates = listOf(rawJson, sanitizeMalformedJson(rawJson)).distinct()

        for (candidate in candidates) {
            try {
                val element = lenientJson.parseToJsonElement(candidate)
                val normalized = normalizeSchema(element)
                return lenientJson.decodeFromJsonElement<CaptureJson>(normalized)
            } catch (e: Throwable) {
                lastError = e
            }
        }

        throw LLMException("Failed to decode JSON response: ${lastError?.message}", lastError)
    }

    /**
     * Handles malformed text fragments seen with tiny models, e.g.
     * `"note":"title":"..."` by rewriting to valid top-level fields.
     */
    private fun sanitizeMalformedJson(raw: String): String {
        return raw
            .replace(
                Regex("\"note\"\\s*:\\s*\"title\"\\s*:\\s*\"", RegexOption.IGNORE_CASE),
                "\"type\":\"note\",\"title\":\""
            )
    }

    private fun normalizeSchema(element: JsonElement): JsonObject {
        val root = element as? JsonObject ?: throw LLMException("Top-level JSON is not an object")
        val noteObj = root["note"] as? JsonObject
        val priorityValue = root["priority"]?.let(::normalizePriority) ?: JsonPrimitive("medium")
        val tagsValue = root["tags"]?.let(::normalizeTags) ?: JsonArray(emptyList())
        val dueDateValue = root["due_date"]?.let(::normalizeDatePlaceholder) ?: JsonNull
        val dueTimeValue = root["due_time"]?.let(::normalizeTimePlaceholder) ?: JsonNull

        val typeValue = when {
            root["type"] != null -> root["type"]!!
            noteObj != null -> JsonPrimitive("note")
            else -> JsonPrimitive("task")
        }

        val titleValue =
            root["title"]
                ?: noteObj?.get("title")
                ?: JsonPrimitive("")

        val bodyValue =
            root["body"]
                ?: noteObj?.get("body")
                ?: JsonNull

        return buildJsonObject {
            put("type", typeValue)
            put("title", titleValue)
            put("body", bodyValue)
            put("due_date", dueDateValue)
            put("due_time", dueTimeValue)
            put("priority", priorityValue)
            put("tags", tagsValue)
            put("confidence", root["confidence"] ?: JsonPrimitive(0.85f))
        }
    }

    private fun normalizePriority(priority: JsonElement): JsonPrimitive {
        if (priority is JsonPrimitive) return priority
        val obj = priority as? JsonObject ?: return JsonPrimitive("medium")
        val candidate = obj["name"] ?: obj["value"] ?: obj["level"] ?: return JsonPrimitive("medium")
        return candidate as? JsonPrimitive ?: JsonPrimitive("medium")
    }

    private fun normalizeTags(tags: JsonElement): JsonArray {
        return when (tags) {
            is JsonArray -> buildJsonArray {
                tags.forEach { item ->
                    val value = (item as? JsonPrimitive)?.let(::primitiveText)
                    if (value != null) add(JsonPrimitive(value))
                }
            }
            is JsonPrimitive -> {
                val content = primitiveText(tags)?.trim().orEmpty()
                if (content.isEmpty()) JsonArray(emptyList()) else JsonArray(listOf(JsonPrimitive(content.removePrefix("#"))))
            }
            else -> JsonArray(emptyList())
        }
    }

    private fun normalizeDatePlaceholder(value: JsonElement): JsonElement {
        val primitive = value as? JsonPrimitive ?: return value
        val text = primitiveText(primitive) ?: return value
        if (text.equals("YYYY-MM-DD", ignoreCase = true)) return JsonNull
        return value
    }

    private fun normalizeTimePlaceholder(value: JsonElement): JsonElement {
        val primitive = value as? JsonPrimitive ?: return value
        val text = primitiveText(primitive) ?: return value
        if (text.equals("HH:MM", ignoreCase = true)) return JsonNull
        return value
    }

    private fun primitiveText(value: JsonPrimitive): String? {
        return runCatching { value.content }.getOrNull()
    }
}

private fun CaptureJson.toDomain(originalText: String): ParsedCapture {
    val safeTitle = title.trim().ifBlank { originalText.trim().ifBlank { "Untitled" } }
    return if (type.lowercase() == "note") {
        ParsedCapture.NoteCapture(
            ParsedNote(
                title = safeTitle,
                body = body ?: originalText,
                tags = tags,
                originalText = originalText,
                confidence = confidence
            )
        )
    } else {
        ParsedCapture.TaskCapture(
            ParsedTask(
                title = safeTitle,
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
