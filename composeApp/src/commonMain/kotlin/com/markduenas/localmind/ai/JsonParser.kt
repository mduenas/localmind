package com.markduenas.localmind.ai

import com.markduenas.localmind.domain.model.ParsedCapture
import com.markduenas.localmind.domain.model.ParsedNote
import com.markduenas.localmind.domain.model.ParsedTask
import com.markduenas.localmind.domain.model.Priority
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.ExperimentalSerializationApi
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

@OptIn(ExperimentalSerializationApi::class)
private val lenientJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    allowTrailingComma = true
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
        val captureJson = decodeCaptureJson(candidates, originalText)
        return captureJson.toDomain(originalText)
    }

    private fun decodeCaptureJson(candidates: List<String>, originalText: String): CaptureJson {
        var lastError: Throwable? = null
        for (candidate in candidates) {
            val variants = listOf(candidate, sanitizeMalformedJson(candidate)).distinct()
            for (variant in variants) {
                try {
                    val element = lenientJson.parseToJsonElement(variant)
                    val normalized = normalizeSchema(element, originalText)
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
            .replace('“', '"')
            .replace('”', '"')
            .replace('’', '\'')
            .replace('‘', '\'')
            .replace('：', ':')
            .replace('，', ',')
            .replace(Regex("(?m)//.*$"), "")
            .replace(Regex("\\bdue\\\\_date\\b"), "due_date")
            .replace(Regex("\\bdue\\\\_time\\b"), "due_time")
            .replace(Regex("([\\{,]\\s*)'([A-Za-z_][A-Za-z0-9_]*)'\\s*:"), "$1\"$2\":")
            .replace(Regex(":\\s*'([^'\\\\]*(?:\\\\.[^'\\\\]*)*)'"), ":\"$1\"")
            .replace(Regex("\\bNone\\b"), "null")
            .replace(Regex("\\bTrue\\b"), "true")
            .replace(Regex("\\bFalse\\b"), "false")
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
            .replace(Regex(",\\s*\\}"), "}")
            .replace(Regex(",\\s*]"), "]")
    }

    private fun normalizeSchema(element: JsonElement, originalText: String): JsonObject {
        val root = element as? JsonObject ?: throw LLMException("Top-level JSON is not an object")
        val noteObj = root["note"] as? JsonObject
        val taskObj = root["task"] as? JsonObject

        val typeValue = normalizeType(
            type = root["type"],
            originalText = originalText,
            hasNoteObject = noteObj != null,
            hasTaskObject = taskObj != null
        )
        val parsedType = primitiveText(typeValue)?.lowercase() ?: "task"

        val rawDueDateValue = (
            root["due_date"] ?: if (parsedType == "task") taskObj?.get("due_date") else null
            )?.let(::normalizeDatePlaceholder) ?: JsonNull
        val rawDueTimeValue = (
            root["due_time"] ?: if (parsedType == "task") taskObj?.get("due_time") else null
            )?.let(::normalizeTimePlaceholder) ?: JsonNull
        val rawPriorityValue = (
            root["priority"] ?: if (parsedType == "task") taskObj?.get("priority") else null
            )?.let(::normalizePriority) ?: JsonPrimitive("medium")
        val tagsValue = (
            root["tags"]
                ?: if (parsedType == "note") noteObj?.get("tags") else taskObj?.get("tags")
            )?.let(::normalizeTags) ?: JsonArray(emptyList())
        val confidenceValue = (
            root["confidence"]
                ?: if (parsedType == "note") noteObj?.get("confidence") else taskObj?.get("confidence")
            )?.let(::normalizeConfidence) ?: JsonPrimitive(0.85f)

        val finalType = adjustTypeForInput(
            requestedType = parsedType,
            originalText = originalText,
            dueDate = rawDueDateValue,
            dueTime = rawDueTimeValue,
        )
        val dueDateValue = if (finalType == "note") JsonNull else rawDueDateValue
        val dueTimeValue = if (finalType == "note") JsonNull else rawDueTimeValue
        val priorityValue = if (finalType == "note") JsonPrimitive("medium") else rawPriorityValue

        val titleValue = when {
            root["title"] != null -> root["title"]!!
            finalType == "note" -> noteObj?.get("title") ?: JsonPrimitive("")
            else -> taskObj?.get("title") ?: JsonPrimitive("")
        }

        val bodyValue = root["body"]
            ?: if (finalType == "note") {
                noteObj?.get("body")
            } else {
                taskObj?.get("body")
            }
            ?: JsonNull

        return buildJsonObject {
            put("type", JsonPrimitive(finalType))
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
        originalText: String,
        hasNoteObject: Boolean,
        hasTaskObject: Boolean,
    ): JsonPrimitive {
        val inferred = inferTypeFromInput(originalText)
        val fallback = when {
            hasNoteObject && !hasTaskObject -> "note"
            hasTaskObject && !hasNoteObject -> "task"
            inferred != "unknown" -> inferred
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

    private fun normalizeConfidence(confidence: JsonElement): JsonPrimitive {
        val fallback = JsonPrimitive(0.85f)
        val primitive = confidence as? JsonPrimitive ?: return fallback
        val raw = primitiveText(primitive)?.trim()?.lowercase() ?: return fallback

        val mapped = when (raw) {
            "high" -> 0.9f
            "medium", "med" -> 0.6f
            "low" -> 0.3f
            else -> {
                val numeric = raw.removeSuffix("%").toFloatOrNull()
                when {
                    numeric == null -> null
                    raw.endsWith("%") -> (numeric / 100f)
                    numeric > 1.0f && numeric <= 100.0f -> (numeric / 100f)
                    else -> numeric
                }
            }
        } ?: return fallback

        return JsonPrimitive(mapped.coerceIn(0.0f, 1.0f))
    }

    private fun adjustTypeForInput(
        requestedType: String,
        originalText: String,
        dueDate: JsonElement,
        dueTime: JsonElement,
    ): String {
        val inferred = inferTypeFromInput(originalText)
        val hasDueDate = dueDate !is JsonNull
        val hasDueTime = dueTime !is JsonNull

        if (requestedType == "note" && (hasDueDate || hasDueTime)) return "task"
        if (inferred == "task" && requestedType == "note") return "task"
        if (inferred == "note" && requestedType == "task" && !hasDueDate && !hasDueTime) return "note"
        return requestedType
    }

    private fun inferTypeFromInput(originalText: String): String {
        val lower = originalText.lowercase()
        val taskPatterns = listOf(
            Regex("\\b(remind|schedule|call|book|submit|pay|email|check|follow up|todo|to do|task)\\b"),
            Regex("\\b(today|tomorrow|tonight|next week|in \\d+ (day|days|week|weeks))\\b"),
            Regex("\\b\\d{1,2}[:.]\\d{2}\\b"),
            Regex("\\b\\d{1,2}(am|pm)\\b"),
        )
        val notePatterns = listOf(
            Regex("\\b(note|idea|thought|quote|takeaway|retro|journal|observation|insight)\\b"),
            Regex("\\b(remember this|learned|i noticed|travel thought)\\b"),
        )

        val taskScore = taskPatterns.count { it.containsMatchIn(lower) }
        val noteScore = notePatterns.count { it.containsMatchIn(lower) }
        return when {
            taskScore > noteScore -> "task"
            noteScore > taskScore -> "note"
            else -> "unknown"
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
            .replace(Regex("<think>.*?</think>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), "")
            .replace(Regex("</?think>", RegexOption.IGNORE_CASE), "")
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
                dueDate = dueDate?.let { parseDateSafe(it) } ?: extractDateFromText(originalText),
                dueTime = dueTime?.let { parseTimeSafe(it) } ?: TimePatterns.extract(originalText),
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

private fun extractDateFromText(text: String): LocalDate? {
    for ((pattern, resolver) in DatePatterns.patterns) {
        val match = pattern.find(text) ?: continue
        val date = resolver(match)
        if (date != null) return date
    }
    return null
}

private fun parseTimeSafe(value: String): LocalTime? {
    val cleaned = value.trim().trim('"')
    if (cleaned.isBlank()) return null

    return runCatching { LocalTime.parse(cleaned) }.getOrNull()
        ?: TimePatterns.extract(cleaned)
}

private fun parsePriority(value: String): Priority {
    return when (value.lowercase()) {
        "high" -> Priority.HIGH
        "low" -> Priority.LOW
        else -> Priority.MEDIUM
    }
}
