package com.markduenas.localmind.ai

import com.markduenas.localmind.ai.benchmark.BenchmarkLiveLogger
import com.markduenas.localmind.domain.model.ParseResult
import com.markduenas.localmind.domain.model.ParsedCapture
import com.markduenas.localmind.domain.model.ParsedTask
import com.markduenas.localmind.domain.model.Priority
import com.markduenas.localmind.domain.usecase.ParseCaptureUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.measureTime
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * 15 realistic voice-capture prompts — a mix of scheduling tasks and notes.
 * Each test validates classification, extracted fields, and correctness.
 *
 * The companion [allPromptsBaseline] runs all 15 through ParseCaptureUseCase
 * (rule-based path) in a single measured pass and prints total elapsed time
 * so we have a baseline number to compare after optimisation.
 */
class ParserBenchmarkTest {

    private val parser = RuleBasedParser()
    private fun today() = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private fun parseAsTask(text: String): ParsedTask {
        val capture = parser.parse(text)
        assertIs<ParsedCapture.TaskCapture>(capture)
        return capture.task
    }

    // ── Scheduling prompts ──────────────────────────────────────────

    @Test
    fun scheduleCallMomTomorrow() {
        val task = parseAsTask("remind me to call mom tomorrow at 6pm")
        assertEquals("Call mom", task.title)
        assertEquals(today().plus(1, DateTimeUnit.DAY), task.dueDate)
        assertEquals(LocalTime(18, 0), task.dueTime)
        assertEquals(Priority.MEDIUM, task.priority)
    }

    @Test
    fun scheduleDentistMarch20() {
        val task = parseAsTask("book dentist appointment March 20 at 10am")
        assertEquals(3, task.dueDate?.monthNumber)
        assertEquals(20, task.dueDate?.dayOfMonth)
        assertEquals(LocalTime(10, 0), task.dueTime)
        assertTrue(task.title.contains("dentist", ignoreCase = true))
    }

    @Test
    fun scheduleGroceriesThisWeekend() {
        val task = parseAsTask("pick up groceries this weekend")
        assertNotNull(task.dueDate)
        assertEquals(DayOfWeek.SATURDAY, task.dueDate!!.dayOfWeek)
        assertTrue(task.title.contains("groceries", ignoreCase = true))
    }

    @Test
    fun scheduleUrgentBugFix() {
        val task = parseAsTask("urgent fix the production database bug #work")
        assertEquals(Priority.HIGH, task.priority)
        assertNull(task.dueDate)
        assertEquals(listOf("work"), task.tags)
        assertTrue(task.title.contains("database", ignoreCase = true) ||
                   task.title.contains("bug", ignoreCase = true))
    }

    @Test
    fun scheduleMeetingNextWeek() {
        val task = parseAsTask("schedule team standup meeting next week at 9:30am")
        assertNotNull(task.dueDate)
        assertEquals(DayOfWeek.MONDAY, task.dueDate!!.dayOfWeek)
        assertEquals(LocalTime(9, 30), task.dueTime)
    }

    @Test
    fun schedulePayBillsIn3Days() {
        val task = parseAsTask("pay electricity bill in 3 days")
        assertEquals(today().plus(3, DateTimeUnit.DAY), task.dueDate)
        assertTrue(task.title.contains("electricity", ignoreCase = true) ||
                   task.title.contains("bill", ignoreCase = true))
    }

    @Test
    fun scheduleSubmitReportToday() {
        val task = parseAsTask("submit quarterly report today before 5pm")
        assertEquals(today(), task.dueDate)
        assertEquals(LocalTime(17, 0), task.dueTime)
    }

    @Test
    fun scheduleLowPriorityCleanup() {
        val task = parseAsTask("clean out the garage no rush #home")
        assertEquals(Priority.LOW, task.priority)
        assertEquals(listOf("home"), task.tags)
        assertNull(task.dueDate)
    }

    @Test
    fun scheduleEmailDayAfterTomorrow() {
        val task = parseAsTask("email the proposal to the client day after tomorrow")
        assertEquals(today().plus(2, DateTimeUnit.DAY), task.dueDate)
        assertTrue(task.title.contains("proposal", ignoreCase = true) ||
                   task.title.contains("email", ignoreCase = true))
    }

    @Test
    fun scheduleMultipleTagsAndDate() {
        val task = parseAsTask("prepare slides for investor pitch in 2 weeks #work #presentation")
        assertEquals(today().plus(14, DateTimeUnit.DAY), task.dueDate)
        assertEquals(listOf("work", "presentation"), task.tags)
    }

    // ── Note prompts ────────────────────────────────────────────────

    @Test
    fun noteRestaurantRecommendation() {
        val capture = parser.parse("the sushi place on Main Street was amazing definitely go back")
        assertIs<ParsedCapture.NoteCapture>(capture)
        assertTrue(capture.note.body.contains("sushi", ignoreCase = true))
    }

    @Test
    fun noteTechObservation() {
        val capture = parser.parse("Kotlin coroutines structured concurrency prevents leaked jobs really well")
        assertIs<ParsedCapture.NoteCapture>(capture)
        assertTrue(capture.note.title.isNotBlank())
    }

    @Test
    fun noteProjectIdea() {
        val capture = parser.parse("a plant watering sensor with raspberry pi would be a cool weekend project")
        assertIs<ParsedCapture.NoteCapture>(capture)
        assertTrue(capture.note.body.contains("raspberry", ignoreCase = true))
    }

    @Test
    fun noteBookRecommendation() {
        val capture = parser.parse("Atomic Habits has great advice on habit stacking really insightful")
        assertIs<ParsedCapture.NoteCapture>(capture)
        assertTrue(capture.note.body.contains("Atomic Habits", ignoreCase = true))
    }

    @Test
    fun noteMeetingTakeaway() {
        val capture = parser.parse("the team agreed we should migrate to Postgres before Q3")
        assertIs<ParsedCapture.NoteCapture>(capture)
        assertTrue(capture.note.body.contains("Postgres", ignoreCase = true))
    }

    // ── Baseline benchmark ──────────────────────────────────────────

    companion object {
        /** All 15 prompts used in the individual tests above. */
        val ALL_PROMPTS = listOf(
            // Scheduling (10)
            "remind me to call mom tomorrow at 6pm",
            "book dentist appointment March 20 at 10am",
            "pick up groceries this weekend",
            "urgent fix the production database bug #work",
            "schedule team standup meeting next week at 9:30am",
            "pay electricity bill in 3 days",
            "submit quarterly report today before 5pm",
            "clean out the garage no rush #home",
            "email the proposal to the client day after tomorrow",
            "prepare slides for investor pitch in 2 weeks #work #presentation",
            // Notes (5)
            "the sushi place on Main Street was amazing definitely go back",
            "Kotlin coroutines structured concurrency prevents leaked jobs really well",
            "a plant watering sensor with raspberry pi would be a cool weekend project",
            "Atomic Habits has great advice on habit stacking really insightful",
            "the team agreed we should migrate to Postgres before Q3",
        )
    }

    /**
     * Runs all 15 prompts through [ParseCaptureUseCase] (rule-based path)
     * and prints the total wall-clock time as a baseline measurement.
     */
    @Test
    fun allPromptsBaseline() = runBlocking {
        val useCase = ParseCaptureUseCase(
            taskParser = com.markduenas.localmind.domain.usecase.StubTaskParser(),
            ruleBasedParser = parser,
            isLLMEnabled = { false },
        )

        var successCount = 0
        val totalTime = measureTime {
            ALL_PROMPTS.forEach { prompt ->
                val result = useCase(prompt)
                assertIs<ParseResult.Success>(result)
                successCount++
            }
        }

        println("=== PARSER BENCHMARK BASELINE ===")
        println("Prompts:    ${ALL_PROMPTS.size}")
        println("Successes:  $successCount")
        println("Total time: ${totalTime.inWholeMilliseconds} ms")
        println("Avg time:   ${totalTime.inWholeMilliseconds / ALL_PROMPTS.size} ms/prompt")
        println("=================================")

        assertEquals(ALL_PROMPTS.size, successCount)
    }

    /**
     * Runs all 200 benchmark fixtures through [RuleBasedParser] and prints
     * per-prompt input → output with timing, then aggregate accuracy/latency stats.
     *
     * This is the canonical JVM rule-based baseline for the full fixture suite.
     */
    @Test
    fun benchmarkAll200() {
        val fixtures = com.markduenas.localmind.ai.benchmark.BenchmarkFixtures.suite

        BenchmarkLiveLogger.header("rule-based", fixtures.suiteVersion, fixtures.prompts.size)

        val evaluations = fixtures.prompts.map { fixture ->
            val started = Clock.System.now()
            val parsed = parser.parse(fixture.prompt)
            val latencyMs = (Clock.System.now() - started).inWholeMilliseconds
            val eval = com.markduenas.localmind.ai.benchmark.BenchmarkScorer.scorePrompt(
                fixture = fixture,
                capture = parsed,
                latencyMs = latencyMs,
                validJson = true,
                fallbackUsed = false,
            )
            BenchmarkLiveLogger.prompt(fixture, eval, parsed)
            eval
        }

        val result = com.markduenas.localmind.ai.benchmark.BenchmarkScorer.aggregate(
            model = "rule-based",
            cacheHit = true,
            evaluations = evaluations,
        )
        BenchmarkLiveLogger.summary(result)

        assertTrue(
            result.classificationAccuracy >= 0.70,
            "Rule-based classification accuracy dropped below 70%: ${result.classificationAccuracy}",
        )
    }
}
