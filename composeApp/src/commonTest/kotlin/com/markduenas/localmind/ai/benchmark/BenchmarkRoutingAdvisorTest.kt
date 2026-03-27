package com.markduenas.localmind.ai.benchmark

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BenchmarkRoutingAdvisorTest {

    @Test
    fun recommendProducesHybridStrategyWhenOnlySomeSegmentsPassThresholds() {
        val baseline = ModelBenchmarkResult(
            model = "rule-based",
            cacheHit = true,
            promptsEvaluated = 2,
            classificationAccuracy = 1.0,
            taskFieldAccuracy = 0.25,
            noteFieldAccuracy = 0.0,
            validJsonRate = 1.0,
            fallbackRate = 0.0,
            latency = LatencyStats(meanMs = 7.0, p50Ms = 7.0, p95Ms = 8.0),
            compositeScore = 0.8,
            evaluations = listOf(
                promptEval(id = "task_01", expectedType = "task", promptWords = 4, fieldScore = 0.25, latencyMs = 6),
                promptEval(id = "note_01", expectedType = "note", promptWords = 14, fieldScore = 0.0, latencyMs = 8),
            ),
        )

        val llm = ModelBenchmarkResult(
            model = "test-llm",
            cacheHit = true,
            promptsEvaluated = 2,
            classificationAccuracy = 1.0,
            taskFieldAccuracy = 0.875,
            noteFieldAccuracy = 0.0,
            validJsonRate = 0.5,
            fallbackRate = 0.5,
            latency = LatencyStats(meanMs = 3000.0, p50Ms = 3000.0, p95Ms = 5000.0),
            compositeScore = 0.7,
            evaluations = listOf(
                promptEval(id = "task_01", expectedType = "task", promptWords = 4, fieldScore = 0.875, latencyMs = 1000),
                promptEval(
                    id = "note_01",
                    expectedType = "note",
                    promptWords = 14,
                    fieldScore = 0.0,
                    latencyMs = 5000,
                    fallbackUsed = true,
                    error = "Timed out waiting for 8000 ms",
                ),
            ),
        )

        val recommendation = BenchmarkRoutingAdvisor.recommend(
            baseline = baseline,
            llmResult = llm,
        )

        assertEquals("hybrid", recommendation.strategy)
        assertEquals(0.5, recommendation.llmCoverageRate)
        assertTrue(recommendation.utilityDeltaVsRule > 0.0)
        assertTrue(recommendation.segments.any { it.segment == "task/short" && it.routeTo == "llm" })
        assertTrue(recommendation.segments.any { it.segment == "note/long" && it.routeTo == "rule-based" })
    }

    private fun promptEval(
        id: String,
        expectedType: String,
        promptWords: Int,
        fieldScore: Double,
        latencyMs: Long,
        fallbackUsed: Boolean = false,
        error: String? = null,
    ): PromptEvaluation {
        return PromptEvaluation(
            id = id,
            expectedType = expectedType,
            actualType = expectedType,
            classificationCorrect = true,
            fieldScore = fieldScore,
            latencyMs = latencyMs,
            promptChars = promptWords * 4,
            promptWords = promptWords,
            validJson = !fallbackUsed,
            fallbackUsed = fallbackUsed,
            error = error,
        )
    }
}
