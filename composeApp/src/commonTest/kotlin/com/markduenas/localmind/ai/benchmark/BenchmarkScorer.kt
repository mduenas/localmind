package com.markduenas.localmind.ai.benchmark

import com.markduenas.localmind.domain.model.ParsedCapture
import com.markduenas.localmind.domain.model.Priority
import kotlin.math.round
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable

@Serializable
data class PromptEvaluation(
    val id: String,
    val expectedType: String,
    val actualType: String,
    val classificationCorrect: Boolean,
    val fieldScore: Double,
    val latencyMs: Long,
    val validJson: Boolean,
    val fallbackUsed: Boolean,
    val error: String? = null,
    val firstError: String? = null,
    val retryError: String? = null,
    val firstResponse: String? = null,
    val retryResponse: String? = null,
)

@Serializable
data class LatencyStats(
    val meanMs: Double,
    val p50Ms: Double,
    val p95Ms: Double,
)

@Serializable
data class ModelBenchmarkResult(
    val model: String,
    val cacheHit: Boolean,
    val promptsEvaluated: Int,
    val classificationAccuracy: Double,
    val taskFieldAccuracy: Double,
    val noteFieldAccuracy: Double,
    val validJsonRate: Double,
    val fallbackRate: Double,
    val latency: LatencyStats,
    val compositeScore: Double,
    val evaluations: List<PromptEvaluation>,
)

object BenchmarkScorer {
    fun scorePrompt(
        fixture: BenchmarkPromptFixture,
        capture: ParsedCapture,
        latencyMs: Long,
        validJson: Boolean,
        fallbackUsed: Boolean,
        error: String? = null,
        firstError: String? = null,
        retryError: String? = null,
        firstResponse: String? = null,
        retryResponse: String? = null,
    ): PromptEvaluation {
        val expectedType = fixture.type.lowercase()
        val actualType = when (capture) {
            is ParsedCapture.TaskCapture -> "task"
            is ParsedCapture.NoteCapture -> "note"
        }
        val classificationCorrect = expectedType == actualType
        val fieldScore = when (capture) {
            is ParsedCapture.TaskCapture -> scoreTask(fixture.expected, capture)
            is ParsedCapture.NoteCapture -> scoreNote(fixture, capture)
        }

        return PromptEvaluation(
            id = fixture.id,
            expectedType = expectedType,
            actualType = actualType,
            classificationCorrect = classificationCorrect,
            fieldScore = fieldScore,
            latencyMs = latencyMs,
            validJson = validJson,
            fallbackUsed = fallbackUsed,
            error = error,
            firstError = firstError,
            retryError = retryError,
            firstResponse = firstResponse,
            retryResponse = retryResponse,
        )
    }

    fun aggregate(model: String, cacheHit: Boolean, evaluations: List<PromptEvaluation>): ModelBenchmarkResult {
        require(evaluations.isNotEmpty()) { "No evaluations to aggregate" }

        val classificationAccuracy = evaluations.count { it.classificationCorrect }.toDouble() / evaluations.size
        val taskEvals = evaluations.filter { it.expectedType == "task" }
        val noteEvals = evaluations.filter { it.expectedType == "note" }
        val taskFieldAccuracy = if (taskEvals.isEmpty()) 0.0 else taskEvals.map { it.fieldScore }.average()
        val noteFieldAccuracy = if (noteEvals.isEmpty()) 0.0 else noteEvals.map { it.fieldScore }.average()
        val validJsonRate = evaluations.count { it.validJson }.toDouble() / evaluations.size
        val fallbackRate = evaluations.count { it.fallbackUsed }.toDouble() / evaluations.size
        val latency = latencyStats(evaluations.map { it.latencyMs })

        // Accuracy-first composite score.
        val accuracyCore = (classificationAccuracy * 0.7) + (((taskFieldAccuracy + noteFieldAccuracy) / 2.0) * 0.3)
        val fallbackPenalty = fallbackRate * 0.1
        val latencyPenalty = minOf(latency.p50Ms / 10000.0, 0.1)
        val composite = (accuracyCore - fallbackPenalty - latencyPenalty).coerceAtLeast(0.0)

        return ModelBenchmarkResult(
            model = model,
            cacheHit = cacheHit,
            promptsEvaluated = evaluations.size,
            classificationAccuracy = round4(classificationAccuracy),
            taskFieldAccuracy = round4(taskFieldAccuracy),
            noteFieldAccuracy = round4(noteFieldAccuracy),
            validJsonRate = round4(validJsonRate),
            fallbackRate = round4(fallbackRate),
            latency = latency,
            compositeScore = round4(composite),
            evaluations = evaluations,
        )
    }

    private fun scoreTask(expected: BenchmarkExpected, capture: ParsedCapture.TaskCapture): Double {
        val checks = mutableListOf<Boolean>()
        checks += titleContains(capture.task.title, expected.titleContains)
        checks += dueDateMatches(expected.dueDateToken, capture.task.dueDate?.toString())
        checks += timeMatches(expected.dueTime, capture.task.dueTime?.toString())
        checks += priorityMatches(expected.priority, capture.task.priority)
        checks += tagsMatch(expected.tags, capture.task.tags)
        return checks.count { it }.toDouble() / checks.size
    }

    private fun scoreNote(fixture: BenchmarkPromptFixture, capture: ParsedCapture.NoteCapture): Double {
        val expected = fixture.expected
        val checks = mutableListOf<Boolean>()
        checks += capture.note.title.isNotBlank()
        checks += bodyContains(capture.note.body, expected.bodyContains)
        checks += retainsInputWhenRequested(expected.bodyRetainsInput, fixture.prompt, capture.note.body)
        checks += tagsMatch(expected.tags, capture.note.tags)
        return checks.count { it }.toDouble() / checks.size
    }

    private fun titleContains(actualTitle: String, expectedTokens: List<String>): Boolean {
        if (expectedTokens.isEmpty()) return true
        val lower = actualTitle.lowercase()
        return expectedTokens.all { lower.contains(it.lowercase()) }
    }

    private fun bodyContains(body: String, expectedTokens: List<String>): Boolean {
        if (expectedTokens.isEmpty()) return true
        val lower = body.lowercase()
        return expectedTokens.all { lower.contains(it.lowercase()) }
    }

    private fun retainsInputWhenRequested(required: Boolean, prompt: String, body: String): Boolean {
        if (!required) return true
        val probe = prompt.split(" ").take(4).joinToString(" ").lowercase()
        return body.lowercase().contains(probe)
    }

    private fun dueDateMatches(expectedToken: String?, actualDate: String?): Boolean {
        if (expectedToken == null) return actualDate == null
        if (actualDate == null) return false

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val actual = runCatching { kotlinx.datetime.LocalDate.parse(actualDate) }.getOrNull() ?: return false

        return when (expectedToken) {
            "TODAY" -> actual == today
            "TODAY_PLUS_1" -> actual == today.plus(1, DateTimeUnit.DAY)
            "TODAY_PLUS_2" -> actual == today.plus(2, DateTimeUnit.DAY)
            "TODAY_PLUS_3" -> actual == today.plus(3, DateTimeUnit.DAY)
            "TODAY_PLUS_14" -> actual == today.plus(14, DateTimeUnit.DAY)
            "THIS_WEEKEND_SATURDAY" -> actual.dayOfWeek == DayOfWeek.SATURDAY
            "NEXT_WEEK_MONDAY" -> actual.dayOfWeek == DayOfWeek.MONDAY && actual > today
            "MONTH_DAY_03_20" -> actual.month == Month.MARCH && actual.day == 20
            else -> false
        }
    }

    private fun timeMatches(expected: String?, actual: String?): Boolean {
        if (expected == null) return actual == null
        if (actual == null) return false
        return normalizeTime(expected) == normalizeTime(actual)
    }

    private fun normalizeTime(value: String): String {
        return value.trim().take(5)
    }

    private fun priorityMatches(expected: String?, actual: Priority): Boolean {
        if (expected == null) return true
        return expected.equals(actual.name, ignoreCase = true)
    }

    private fun tagsMatch(expectedTags: List<String>?, actualTags: List<String>): Boolean {
        if (expectedTags == null) return true
        val actualSet = actualTags.map { it.lowercase() }.toSet()
        return expectedTags.all { actualSet.contains(it.lowercase()) }
    }

    private fun latencyStats(values: List<Long>): LatencyStats {
        val sorted = values.sorted()
        val mean = sorted.average()
        val p50 = percentile(sorted, 0.50)
        val p95 = percentile(sorted, 0.95)
        return LatencyStats(
            meanMs = round4(mean),
            p50Ms = round4(p50),
            p95Ms = round4(p95),
        )
    }

    private fun percentile(sorted: List<Long>, p: Double): Double {
        if (sorted.isEmpty()) return 0.0
        val idx = ((sorted.size - 1) * p).toInt()
        return sorted[idx].toDouble()
    }

    private fun round4(value: Double): Double = round(value * 10000.0) / 10000.0
}
