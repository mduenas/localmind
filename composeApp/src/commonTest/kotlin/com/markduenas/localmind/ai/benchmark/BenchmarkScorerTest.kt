package com.markduenas.localmind.ai.benchmark

import com.markduenas.localmind.domain.model.ParsedCapture
import com.markduenas.localmind.domain.model.ParsedNote
import com.markduenas.localmind.domain.model.ParsedTask
import com.markduenas.localmind.domain.model.Priority
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

class BenchmarkScorerTest {

    @Test
    fun taskPromptScoresAsExpectedWhenFieldsMatch() {
        val fixture = BenchmarkFixtures.suite.prompts.first { it.id == "task_01" }
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val capture = ParsedCapture.TaskCapture(
            task = ParsedTask(
                title = "Call mom",
                dueDate = today.plus(1, DateTimeUnit.DAY),
                dueTime = kotlinx.datetime.LocalTime.parse("18:00"),
                priority = Priority.MEDIUM,
                tags = emptyList(),
                originalText = fixture.prompt,
                confidence = 0.9f,
                suggestedEdits = null,
            )
        )

        val evaluation = BenchmarkScorer.scorePrompt(
            fixture = fixture,
            capture = capture,
            latencyMs = 120,
            validJson = true,
            fallbackUsed = false,
        )

        assertTrue(evaluation.classificationCorrect)
        assertTrue(evaluation.fieldScore >= 0.99)
    }

    @Test
    fun notePromptScoresAsExpectedWhenBodyRetained() {
        val fixture = BenchmarkFixtures.suite.prompts.first { it.id == "note_02" }
        val capture = ParsedCapture.NoteCapture(
            note = ParsedNote(
                title = "Coroutine thought",
                body = fixture.prompt,
                tags = emptyList(),
                originalText = fixture.prompt,
                confidence = 0.85f,
            )
        )

        val evaluation = BenchmarkScorer.scorePrompt(
            fixture = fixture,
            capture = capture,
            latencyMs = 90,
            validJson = true,
            fallbackUsed = false,
        )

        assertTrue(evaluation.classificationCorrect)
        assertTrue(evaluation.fieldScore >= 0.99)
    }

    @Test
    fun aggregateIncludesTimeoutAndPromptLengthMetrics() {
        val shortTaskFixture = BenchmarkFixtures.suite.prompts.first { it.id == "task_03" }
        val longNoteFixture = BenchmarkFixtures.suite.prompts.first { it.id == "note_03" }
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        val taskEvaluation = BenchmarkScorer.scorePrompt(
            fixture = shortTaskFixture,
            capture = ParsedCapture.TaskCapture(
                task = ParsedTask(
                    title = "Pick up groceries",
                    dueDate = today.plus(1, DateTimeUnit.DAY),
                    dueTime = null,
                    priority = Priority.MEDIUM,
                    tags = emptyList(),
                    originalText = shortTaskFixture.prompt,
                    confidence = 0.9f,
                    suggestedEdits = null,
                )
            ),
            latencyMs = 1200,
            validJson = false,
            fallbackUsed = true,
            error = "Timed out waiting for 8000 ms",
        )

        val noteEvaluation = BenchmarkScorer.scorePrompt(
            fixture = longNoteFixture,
            capture = ParsedCapture.NoteCapture(
                note = ParsedNote(
                    title = "Plant watering project",
                    body = longNoteFixture.prompt,
                    tags = emptyList(),
                    originalText = longNoteFixture.prompt,
                    confidence = 0.9f,
                )
            ),
            latencyMs = 200,
            validJson = true,
            fallbackUsed = false,
        )

        val aggregate = BenchmarkScorer.aggregate(
            model = "test-model",
            cacheHit = true,
            evaluations = listOf(taskEvaluation, noteEvaluation),
        )

        assertEquals(0.5, aggregate.timeoutRate)
        assertEquals(0.5, aggregate.fallbackRate)
        assertEquals(1200.0, aggregate.taskLatency.meanMs)
        assertEquals(200.0, aggregate.noteLatency.meanMs)
        assertTrue(aggregate.latencyByPromptLength.any { it.bucket == "short" })
        assertTrue(aggregate.latencyByPromptLength.any { it.bucket == "long" })
    }
}
