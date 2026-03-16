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
import kotlin.math.min

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
        return extractJsonCandidates(raw).firstOrNull()
            ?: throw LLMException("No JSON object found in LLM response")
    }

    fun parse(jsonString: String, originalText: String): ParsedCapture {
        val candidates = extractJsonCandidates(jsonString)
        if (candidates.isEmpty()) throw LLMException("No JSON object found in LLM response")
        val captureJson = decodeCaptureJson(candidates)
        return captureJson.toDomain(originalText)
    }

    private fun decodeCaptureJson(candidates: List<String>): CaptureJson {
        var lastError: Throwable? = null
        for (candidate in candidates) {
            val variants = listOf(candidate, sanitizeMalformedJson(candidate)).distinct()
            for (variant in variants) {
                try {
                    val element = lenientJson.parseToJsonElement(variant)
                    val normalized = normalizeSchema(element)
                    return lenientJson.decodeFromJsonElement<CaptureJson>(normalized)
                } catch (e: Throwable) {
                    lastError = e
                }
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
            // Rewrites malformed comma + object key style: ... ,{"note":{...}} -> ...,"note":{...}
            .replace(
                Regex(",\\s*\\{\\s*\"(type|task|note|title|body|due_date|due_time|priority|tags|confidence)\"\\s*:", RegexOption.IGNORE_CASE),
                ",\"$1\":"
            )
            // Quote bare keys like: {body:null} or , body:"..."
            .replace(
                Regex("([\\{,])\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*:"),
                "$1\"$2\":"
            )
            // Flatten nested type object: "type":{"type":"task"} -> "type":"task"
            .replace(
                Regex("\"type\"\\s*:\\s*\\{\\s*\"(?:type|value|name)\"\\s*:\\s*\"(task|note)\"\\s*\\}", RegexOption.IGNORE_CASE),
                "\"type\":\"$1\""
            )
            // Remove trailing commas before closing braces.
            .replace(Regex(",\\s*}"), "}")
    }

    private fun normalizeSchema(element: JsonElement): JsonObject {
        val root = element as? JsonObject ?: throw LLMException("Top-level JSON is not an object")
        val noteObj = root["note"] as? JsonObject
        val taskObj = root["task"] as? JsonObject

        val typeValue = normalizeType(
            type = root["type"],
            hasNoteObject = noteObj != null,
            hasTaskObject = taskObj != null
        )
        val parsedType = primitiveText(typeValue)?.lowercase() ?: "task"

        val dueDateValue = (
            root["due_date"] ?: if (parsedType == "task") taskObj?.get("due_date") else null
            )?.let(::normalizeDatePlaceholder) ?: JsonNull
        val dueTimeValue = (
            root["due_time"] ?: if (parsedType == "task") taskObj?.get("due_time") else null
            )?.let(::normalizeTimePlaceholder) ?: JsonNull
        val priorityValue = (
            root["priority"] ?: if (parsedType == "task") taskObj?.get("priority") else null
            )?.let(::normalizePriority) ?: JsonPrimitive("medium")
        val tagsValue = (
            root["tags"]
                ?: if (parsedType == "note") noteObj?.get("tags") else taskObj?.get("tags")
            )?.let(::normalizeTags) ?: JsonArray(emptyList())
        val confidenceValue = (
            root["confidence"]
                ?: if (parsedType == "note") noteObj?.get("confidence") else taskObj?.get("confidence")
            ) ?: JsonPrimitive(0.85f)

        val titleValue = when {
            root["title"] != null -> root["title"]!!
            parsedType == "note" -> noteObj?.get("title") ?: JsonPrimitive("")
            else -> taskObj?.get("title") ?: JsonPrimitive("")
        }

        val bodyValue = root["body"]
            ?: if (parsedType == "note") {
                noteObj?.get("body")
            } else {
                taskObj?.get("body")
            }
            ?: JsonNull

        return buildJsonObject {
            put("type", typeValue)
            put("title", titleValue)
            put("body", bodyValue)
            put("due_date", dueDateValue)
            put("due_time", dueTimeValue)
            put("priority", priorityValue)
            put("tags", tagsValue)
            put("confidence", confidenceValue)
        }
    }

    private fun normalizeType(
        type: JsonElement?,
        hasNoteObject: Boolean,
        hasTaskObject: Boolean,
    ): JsonPrimitive {
        val fallback = when {
            hasNoteObject && !hasTaskObject -> "note"
            else -> "task"
        }
        val explicit = when (type) {
            is JsonPrimitive -> primitiveText(type)?.lowercase()
            is JsonObject -> {
                val nestedType = type["type"] ?: type["name"] ?: type["value"]
                (nestedType as? JsonPrimitive)?.let(::primitiveText)?.lowercase()
            }
            else -> null
        }
        return when (explicit) {
            "task", "note" -> JsonPrimitive(explicit)
            else -> JsonPrimitive(fallback)
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

    private fun extractJsonCandidates(raw: String): List<String> {
        val stripped = stripKnownArtifacts(raw)
        if (stripped.isBlank()) return emptyList()

        val candidates = LinkedHashSet<String>()
        for (index in stripped.indices) {
            if (stripped[index] != '{') continue
            val end = findMatchingBrace(stripped, index) ?: continue
            val candidate = stripped.substring(index, end + 1).trim()
            if (candidate.isNotEmpty()) candidates += candidate
        }

        return candidates.sortedByDescending(::scoreCandidate)
    }

    private fun stripKnownArtifacts(raw: String): String {
        return raw
            .replace(Regex("```json\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("```\\s*"), "")
            .replace(Regex("<\\s*end_of_turn\\s*>", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun scoreCandidate(candidate: String): Int {
        val keyScore = KNOWN_SCHEMA_KEYS.sumOf { key ->
            if (candidate.contains("\"$key\"", ignoreCase = true)) 10 else 0
        }
        val explicitTypeBonus = if (Regex("\"type\"\\s*:", RegexOption.IGNORE_CASE).containsMatchIn(candidate)) 120 else 0
        val explicitTitleBonus = if (Regex("\"title\"\\s*:", RegexOption.IGNORE_CASE).containsMatchIn(candidate)) 20 else 0
        return keyScore + min(candidate.length, 200) + explicitTypeBonus + explicitTitleBonus
    }

    private fun findMatchingBrace(text: String, start: Int): Int? {
        var depth = 0
        var inString = false
        var escaped = false

        for (index in start until text.length) {
            val c = text[index]
            if (inString) {
                if (escaped) {
                    escaped = false
                } else if (c == '\\') {
                    escaped = true
                } else if (c == '"') {
                    inString = false
                }
                continue
            }

            when (c) {
                '"' -> inString = true
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return index
                    if (depth < 0) return null
                }
            }
        }
        return null
    }

    private val KNOWN_SCHEMA_KEYS = listOf(
        "type", "title", "body", "due_date", "due_time", "priority", "tags", "confidence", "note", "task"
    )
}

private fun CaptureJson.toDomain(originalText: String): ParsedCapture {
    val trimmedTitle = title.trim().trim('"')
    val isPlaceholderTitle = trimmedTitle == "..."
    val safeTitle = if (trimmedTitle.isBlank() || isPlaceholderTitle) {
        originalText.trim().ifBlank { "Untitled" }
    } else {
        trimmedTitle
    }
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
