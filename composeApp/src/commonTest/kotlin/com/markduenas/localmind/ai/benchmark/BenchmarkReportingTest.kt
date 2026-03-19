package com.markduenas.localmind.ai.benchmark

import kotlin.test.Test
import kotlin.test.assertTrue

class BenchmarkReportingTest {

    @Test
    fun detailedMarkdownIncludesModelMetricsAndPromptTable() {
        val report = BenchmarkSuiteReport(
            generatedAt = "2026-03-18T00:00:00",
            suiteVersion = "v1.0.0",
            benchmarkedModels = listOf("test-model"),
            baselineModel = "rule-based",
            results = listOf(
                ModelBenchmarkResult(
                    model = "test-model",
                    cacheHit = true,
                    promptsEvaluated = 1,
                    classificationAccuracy = 1.0,
                    taskFieldAccuracy = 1.0,
                    noteFieldAccuracy = 1.0,
                    validJsonRate = 0.0,
                    fallbackRate = 1.0,
                    latency = LatencyStats(meanMs = 100.0, p50Ms = 100.0, p95Ms = 100.0),
                    compositeScore = 0.9,
                    evaluations = listOf(
                        PromptEvaluation(
                            id = "task_01",
                            expectedType = "task",
                            actualType = "task",
                            classificationCorrect = true,
                            fieldScore = 1.0,
                            latencyMs = 100,
                            validJson = false,
                            fallbackUsed = true,
                            error = "No JSON object found in LLM response",
                        )
                    ),
                )
            ),
            suggestions = emptyList(),
        )

        val markdown = BenchmarkReportRenderer.toDetailedMarkdown(report)

        assertTrue(markdown.contains("# LocalMind LLM Benchmark Detailed Report"))
        assertTrue(markdown.contains("## Model Metrics"))
        assertTrue(markdown.contains("## Model: test-model"))
        assertTrue(markdown.contains("### Error Breakdown"))
        assertTrue(markdown.contains("### Prompt Evaluations"))
        assertTrue(markdown.contains("task_01"))
    }
}
