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
                    timeoutRate = 1.0,
                    latency = LatencyStats(meanMs = 100.0, p50Ms = 100.0, p95Ms = 100.0),
                    taskLatency = LatencyStats(meanMs = 110.0, p50Ms = 110.0, p95Ms = 110.0),
                    noteLatency = LatencyStats(meanMs = 90.0, p50Ms = 90.0, p95Ms = 90.0),
                    latencyByPromptLength = listOf(
                        PromptLengthLatency(
                            bucket = "short",
                            prompts = 1,
                            meanMs = 100.0,
                            p50Ms = 100.0,
                            p95Ms = 100.0,
                            fallbackRate = 1.0,
                            timeoutRate = 1.0,
                            utilityScore = 0.9,
                        )
                    ),
                    compositeScore = 0.9,
                    evaluations = listOf(
                        PromptEvaluation(
                            id = "task_01",
                            expectedType = "task",
                            actualType = "task",
                            classificationCorrect = true,
                            fieldScore = 1.0,
                            latencyMs = 100,
                            promptChars = 35,
                            promptWords = 7,
                            validJson = false,
                            fallbackUsed = true,
                            error = "No JSON object found in LLM response",
                        )
                    ),
                )
            ),
            suggestions = emptyList(),
            routingRecommendations = listOf(
                RoutingRecommendation(
                    model = "test-model",
                    strategy = "rule_only",
                    llmCoverageRate = 0.0,
                    projectedUtility = 0.9,
                    projectedMeanLatencyMs = 10.0,
                    projectedFallbackRate = 0.0,
                    projectedTimeoutRate = 0.0,
                    utilityDeltaVsRule = 0.0,
                    latencyDeltaVsRuleMs = 0.0,
                    thresholds = RoutingThresholds(),
                    segments = listOf(
                        RoutingSegmentRecommendation(
                            segment = "task/medium",
                            prompts = 1,
                            routeTo = "rule-based",
                            llmUtility = 0.9,
                            ruleUtility = 0.9,
                            utilityDelta = 0.0,
                            llmMeanLatencyMs = 100.0,
                            ruleMeanLatencyMs = 2.0,
                            llmP50LatencyMs = 100.0,
                            ruleP50LatencyMs = 2.0,
                            llmFallbackRate = 1.0,
                            llmTimeoutRate = 1.0,
                            reason = "Fallback rate 1.0 above threshold 0.2",
                        )
                    ),
                    summary = "Keep all prompts on rule-based parser for this model.",
                )
            ),
        )

        val markdown = BenchmarkReportRenderer.toDetailedMarkdown(report)
        val routingMarkdown = BenchmarkReportRenderer.toRoutingMarkdown(report)

        assertTrue(markdown.contains("# LocalMind LLM Benchmark Detailed Report"))
        assertTrue(markdown.contains("## Model Metrics"))
        assertTrue(markdown.contains("## Model: test-model"))
        assertTrue(markdown.contains("### Error Breakdown"))
        assertTrue(markdown.contains("### Prompt Evaluations"))
        assertTrue(markdown.contains("### Latency By Prompt Type"))
        assertTrue(markdown.contains("### Latency By Prompt Length"))
        assertTrue(markdown.contains("### Routing Recommendation"))
        assertTrue(markdown.contains("task_01"))

        assertTrue(routingMarkdown.contains("# LocalMind Routing Recommendations"))
        assertTrue(routingMarkdown.contains("## Model: test-model"))
        assertTrue(routingMarkdown.contains("task/medium"))
    }
}
