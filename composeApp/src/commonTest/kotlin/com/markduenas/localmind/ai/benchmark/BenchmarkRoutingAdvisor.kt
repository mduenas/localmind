package com.markduenas.localmind.ai.benchmark

import kotlin.math.round
import kotlinx.serialization.Serializable

@Serializable
data class RoutingThresholds(
    val minUtilityGain: Double = 0.03,
    val maxFallbackRate: Double = 0.2,
    val maxTimeoutRate: Double = 0.1,
    val maxP50LatencyMs: Double = 4500.0,
)

@Serializable
data class RoutingSegmentRecommendation(
    val segment: String,
    val prompts: Int,
    val routeTo: String,
    val llmUtility: Double,
    val ruleUtility: Double,
    val utilityDelta: Double,
    val llmMeanLatencyMs: Double,
    val ruleMeanLatencyMs: Double,
    val llmP50LatencyMs: Double,
    val ruleP50LatencyMs: Double,
    val llmFallbackRate: Double,
    val llmTimeoutRate: Double,
    val reason: String,
)

@Serializable
data class RoutingRecommendation(
    val model: String,
    val strategy: String,
    val llmCoverageRate: Double,
    val projectedUtility: Double,
    val projectedMeanLatencyMs: Double,
    val projectedFallbackRate: Double,
    val projectedTimeoutRate: Double,
    val utilityDeltaVsRule: Double,
    val latencyDeltaVsRuleMs: Double,
    val thresholds: RoutingThresholds,
    val segments: List<RoutingSegmentRecommendation>,
    val summary: String,
)

object BenchmarkRoutingAdvisor {
    fun recommend(
        baseline: ModelBenchmarkResult,
        llmResult: ModelBenchmarkResult,
        thresholds: RoutingThresholds = RoutingThresholds(),
    ): RoutingRecommendation {
        val baselineById = baseline.evaluations.associateBy { it.id }
        val paired = llmResult.evaluations.mapNotNull { llmEval ->
            val ruleEval = baselineById[llmEval.id] ?: return@mapNotNull null
            PairedPrompt(
                rule = ruleEval,
                llm = llmEval,
                segment = segmentFor(ruleEval),
            )
        }
        require(paired.isNotEmpty()) { "No overlapping prompt IDs between baseline and ${llmResult.model}" }

        val segments = paired
            .groupBy { it.segment }
            .map { (segment, prompts) -> segmentRecommendation(segment, prompts, thresholds) }
            .sortedWith(compareBy({ segmentTypeOrder(it.segment) }, { segmentLengthOrder(it.segment) }, { it.segment }))

        val routeBySegment = segments.associateBy { it.segment }
        val projected = paired.map { pair ->
            val decision = routeBySegment[pair.segment]
            if (decision?.routeTo == ROUTE_LLM) pair.llm else pair.rule
        }

        val llmCoverageRate = round4(segments.sumOf { if (it.routeTo == ROUTE_LLM) it.prompts else 0 }.toDouble() / paired.size)
        val projectedUtility = round4(projected.map { it.utilityScore() }.average())
        val projectedMeanLatencyMs = round4(projected.map { it.latencyMs.toDouble() }.average())
        val projectedFallbackRate = round4(projected.count { it.fallbackUsed }.toDouble() / projected.size)
        val projectedTimeoutRate = round4(projected.count { it.timedOut() }.toDouble() / projected.size)

        val ruleUtility = baseline.evaluations.map { it.utilityScore() }.average()
        val ruleMeanLatencyMs = baseline.evaluations.map { it.latencyMs.toDouble() }.average()
        val utilityDeltaVsRule = round4(projectedUtility - ruleUtility)
        val latencyDeltaVsRuleMs = round4(projectedMeanLatencyMs - ruleMeanLatencyMs)

        val strategy = when {
            llmCoverageRate <= 0.0 -> "rule_only"
            llmCoverageRate >= 1.0 -> "llm_only"
            else -> "hybrid"
        }

        val summary = when (strategy) {
            "rule_only" -> "Keep all prompts on rule-based parser for this model."
            "llm_only" -> "Route all prompts to ${llmResult.model} for this benchmark profile."
            else -> "Route ${percent(llmCoverageRate)} of prompts to ${llmResult.model}, keep the rest rule-based."
        }

        return RoutingRecommendation(
            model = llmResult.model,
            strategy = strategy,
            llmCoverageRate = llmCoverageRate,
            projectedUtility = projectedUtility,
            projectedMeanLatencyMs = projectedMeanLatencyMs,
            projectedFallbackRate = projectedFallbackRate,
            projectedTimeoutRate = projectedTimeoutRate,
            utilityDeltaVsRule = utilityDeltaVsRule,
            latencyDeltaVsRuleMs = latencyDeltaVsRuleMs,
            thresholds = thresholds,
            segments = segments,
            summary = summary,
        )
    }

    private fun segmentRecommendation(
        segment: String,
        prompts: List<PairedPrompt>,
        thresholds: RoutingThresholds,
    ): RoutingSegmentRecommendation {
        val llmEvals = prompts.map { it.llm }
        val ruleEvals = prompts.map { it.rule }

        val llmUtility = llmEvals.map { it.utilityScore() }.average()
        val ruleUtility = ruleEvals.map { it.utilityScore() }.average()
        val utilityDelta = llmUtility - ruleUtility
        val llmFallbackRate = llmEvals.count { it.fallbackUsed }.toDouble() / llmEvals.size
        val llmTimeoutRate = llmEvals.count { it.timedOut() }.toDouble() / llmEvals.size
        val llmP50 = percentile(llmEvals.map { it.latencyMs.toDouble() }, 0.50)
        val ruleP50 = percentile(ruleEvals.map { it.latencyMs.toDouble() }, 0.50)

        val routeToLlm = utilityDelta >= thresholds.minUtilityGain &&
            llmFallbackRate <= thresholds.maxFallbackRate &&
            llmTimeoutRate <= thresholds.maxTimeoutRate &&
            llmP50 <= thresholds.maxP50LatencyMs

        val reason = when {
            utilityDelta < thresholds.minUtilityGain ->
                "Utility delta ${signed(utilityDelta)} below threshold +${thresholds.minUtilityGain}"
            llmFallbackRate > thresholds.maxFallbackRate ->
                "Fallback rate ${round4(llmFallbackRate)} above threshold ${thresholds.maxFallbackRate}"
            llmTimeoutRate > thresholds.maxTimeoutRate ->
                "Timeout rate ${round4(llmTimeoutRate)} above threshold ${thresholds.maxTimeoutRate}"
            llmP50 > thresholds.maxP50LatencyMs ->
                "P50 latency ${round4(llmP50)}ms above threshold ${thresholds.maxP50LatencyMs}ms"
            else -> "LLM passes quality/reliability/latency thresholds"
        }

        return RoutingSegmentRecommendation(
            segment = segment,
            prompts = prompts.size,
            routeTo = if (routeToLlm) ROUTE_LLM else ROUTE_RULE,
            llmUtility = round4(llmUtility),
            ruleUtility = round4(ruleUtility),
            utilityDelta = round4(utilityDelta),
            llmMeanLatencyMs = round4(llmEvals.map { it.latencyMs.toDouble() }.average()),
            ruleMeanLatencyMs = round4(ruleEvals.map { it.latencyMs.toDouble() }.average()),
            llmP50LatencyMs = round4(llmP50),
            ruleP50LatencyMs = round4(ruleP50),
            llmFallbackRate = round4(llmFallbackRate),
            llmTimeoutRate = round4(llmTimeoutRate),
            reason = reason,
        )
    }

    private fun segmentFor(evaluation: PromptEvaluation): String {
        val bucket = BenchmarkScorer.promptLengthBucket(evaluation.promptWords)
        return "${evaluation.expectedType}/$bucket"
    }

    private fun segmentTypeOrder(segment: String): Int {
        return when (segment.substringBefore('/')) {
            "task" -> 0
            "note" -> 1
            else -> 2
        }
    }

    private fun segmentLengthOrder(segment: String): Int {
        return when (segment.substringAfter('/', missingDelimiterValue = "")) {
            "short" -> 0
            "medium" -> 1
            "long" -> 2
            else -> 3
        }
    }

    private fun percentile(values: List<Double>, p: Double): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val idx = ((sorted.size - 1) * p).toInt()
        return sorted[idx]
    }

    private fun round4(value: Double): Double = round(value * 10000.0) / 10000.0

    private fun percent(value: Double): String = "${round4(value * 100.0)}%"

    private fun signed(value: Double): String {
        val rounded = round4(value)
        return if (rounded >= 0) "+$rounded" else rounded.toString()
    }

    private data class PairedPrompt(
        val rule: PromptEvaluation,
        val llm: PromptEvaluation,
        val segment: String,
    )

    private const val ROUTE_LLM = "llm"
    private const val ROUTE_RULE = "rule-based"
}
