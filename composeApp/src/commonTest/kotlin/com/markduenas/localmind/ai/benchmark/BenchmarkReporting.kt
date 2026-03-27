package com.markduenas.localmind.ai.benchmark

import com.markduenas.localmind.ai.AIConfig
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ModelCatalogEntry(
    val slug: String,
    val approxSizeMb: Int,
    val source: String,
)

@Serializable
data class ModelSuggestion(
    val slug: String,
    val approxSizeMb: Int,
    val reason: String,
)

@Serializable
data class BenchmarkSuiteReport(
    val generatedAt: String,
    val suiteVersion: String,
    val benchmarkedModels: List<String>,
    val baselineModel: String,
    val results: List<ModelBenchmarkResult>,
    val suggestions: List<ModelSuggestion>,
    val routingRecommendations: List<RoutingRecommendation> = emptyList(),
)

object BenchmarkModelCatalog {
    val primaryCandidates = listOf(
        AIConfig.TINY_LLM_MODEL,
        AIConfig.FUNCTION_TINY_LLM_MODEL,
    )

    // Snapshot-style shortlist for selecting a third benchmark model.
    val thirdModelProbeCandidates = listOf(
        "llama3.2-1b",
        "qwen2.5-1.5b",
        "phi-3.5-mini",
        "gemma2-2b",
    )

    // Local + catalog snapshot used for suggestion ranking.
    val catalog = listOf(
        ModelCatalogEntry(slug = AIConfig.TINY_LLM_MODEL, approxSizeMb = 200, source = "cactus"),
        ModelCatalogEntry(slug = AIConfig.FUNCTION_TINY_LLM_MODEL, approxSizeMb = 400, source = "cactus"),
        ModelCatalogEntry(slug = "llama3.2-1b", approxSizeMb = 1300, source = "cactus"),
        ModelCatalogEntry(slug = "qwen2.5-1.5b", approxSizeMb = 1500, source = "cactus"),
        ModelCatalogEntry(slug = "phi-3.5-mini", approxSizeMb = 1900, source = "cactus"),
        ModelCatalogEntry(slug = "gemma2-2b", approxSizeMb = 2000, source = "cactus"),
    )

    fun suggestModels(
        installedModels: Set<String>,
        benchmarkedModels: Set<String>,
        limit: Int = 3,
    ): List<ModelSuggestion> {
        return catalog
            .filterNot { installedModels.contains(it.slug) }
            .filterNot { benchmarkedModels.contains(it.slug) }
            .sortedWith(compareBy<ModelCatalogEntry> { it.approxSizeMb }.thenBy { it.slug })
            .take(limit)
            .map {
                ModelSuggestion(
                    slug = it.slug,
                    approxSizeMb = it.approxSizeMb,
                    reason = "Small/likely-fast candidate not yet installed",
                )
            }
    }
}

object BenchmarkReportRenderer {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun nowIsoString(): String {
        return Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toString()
    }

    fun toJson(report: BenchmarkSuiteReport): String = json.encodeToString(report)

    fun toSummaryMarkdown(report: BenchmarkSuiteReport): String {
        val ranked = report.results.sortedByDescending { it.compositeScore }
        val winner = ranked.firstOrNull()

        val lines = mutableListOf<String>()
        lines += "# LocalMind LLM Benchmark"
        lines += ""
        lines += "- Generated: ${report.generatedAt}"
        lines += "- Fixture suite: ${report.suiteVersion}"
        lines += "- Baseline: ${report.baselineModel}"
        lines += ""
        if (winner != null) {
            lines += "## Winner"
            lines += "${winner.model} (composite=${winner.compositeScore})"
            lines += ""
        }

        lines += "## Ranking"
        lines += "| Rank | Model | Composite | Classification | Task Fields | Note Fields | Fallback | P50 (ms) | Cache Hit |"
        lines += "|---|---|---:|---:|---:|---:|---:|---:|---|"
        ranked.forEachIndexed { index, result ->
            lines += "| ${index + 1} | ${result.model} | ${result.compositeScore} | ${result.classificationAccuracy} | ${result.taskFieldAccuracy} | ${result.noteFieldAccuracy} | ${result.fallbackRate} | ${result.latency.p50Ms} | ${result.cacheHit} |"
        }

        lines += ""
        lines += "## Routing Recommendations"
        if (report.routingRecommendations.isEmpty()) {
            lines += "No routing recommendations generated."
        } else {
            lines += "| Model | Strategy | LLM Coverage | Utility Delta vs Rule | Mean Latency Delta (ms) | Summary |"
            lines += "|---|---|---:|---:|---:|---|"
            report.routingRecommendations
                .sortedBy { it.model }
                .forEach { recommendation ->
                    lines += "| ${recommendation.model} | ${recommendation.strategy} | ${recommendation.llmCoverageRate} | ${recommendation.utilityDeltaVsRule} | ${recommendation.latencyDeltaVsRuleMs} | ${mdCell(recommendation.summary, maxLen = 120)} |"
                }
        }

        return lines.joinToString("\n")
    }

    fun toSuggestionsMarkdown(report: BenchmarkSuiteReport): String {
        val lines = mutableListOf<String>()
        lines += "# Suggested Models To Try"
        lines += ""

        if (report.suggestions.isEmpty()) {
            lines += "No new small-model suggestions right now."
            return lines.joinToString("\n")
        }

        report.suggestions.forEachIndexed { index, suggestion ->
            lines += "${index + 1}. ${suggestion.slug} (~${suggestion.approxSizeMb} MB)"
            lines += "   - ${suggestion.reason}"
        }

        return lines.joinToString("\n")
    }

    fun toDetailedMarkdown(report: BenchmarkSuiteReport): String {
        val ranked = report.results.sortedByDescending { it.compositeScore }
        val lines = mutableListOf<String>()

        lines += "# LocalMind LLM Benchmark Detailed Report"
        lines += ""
        lines += "- Generated: ${report.generatedAt}"
        lines += "- Fixture suite: ${report.suiteVersion}"
        lines += "- Baseline: ${report.baselineModel}"
        lines += ""
        lines += "## Model Metrics"
        lines += "| Model | Composite | Prompts | Classification | Task Fields | Note Fields | Valid JSON | Fallback | Timeout | Mean (ms) | P50 (ms) | P95 (ms) | Cache Hit |"
        lines += "|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|"
        ranked.forEach { result ->
            lines += "| ${result.model} | ${result.compositeScore} | ${result.promptsEvaluated} | ${result.classificationAccuracy} | ${result.taskFieldAccuracy} | ${result.noteFieldAccuracy} | ${result.validJsonRate} | ${result.fallbackRate} | ${result.timeoutRate} | ${result.latency.meanMs} | ${result.latency.p50Ms} | ${result.latency.p95Ms} | ${result.cacheHit} |"
        }

        ranked.forEach { result ->
            lines += ""
            lines += "## Model: ${result.model}"
            lines += ""
            lines += "### Error Breakdown"

            val groupedErrors = result.evaluations
                .mapNotNull { it.error?.trim()?.takeIf { msg -> msg.isNotEmpty() } }
                .groupingBy { it }
                .eachCount()
                .toList()
                .sortedByDescending { (_, count) -> count }

            if (groupedErrors.isEmpty()) {
                lines += "No errors."
            } else {
                lines += "| Error | Count |"
                lines += "|---|---:|"
                groupedErrors.forEach { (error, count) ->
                    lines += "| ${mdCell(error, maxLen = 120)} | $count |"
                }
            }

            lines += ""
            lines += "### Prompt Evaluations"
            lines += "| Prompt | Expected | Actual | Classification OK | Field Score | Latency (ms) | Valid JSON | Fallback | Error |"
            lines += "|---|---|---|---|---:|---:|---|---|---|"
            result.evaluations
                .sortedBy { it.id }
                .forEach { evaluation ->
                    lines += "| ${mdCell(evaluation.id)} | ${evaluation.expectedType} | ${evaluation.actualType} | ${evaluation.classificationCorrect} | ${evaluation.fieldScore} | ${evaluation.latencyMs} | ${evaluation.validJson} | ${evaluation.fallbackUsed} | ${mdCell(evaluation.error.orEmpty(), maxLen = 120)} |"
                }

            lines += ""
            lines += "### Invalid JSON Raw Output Samples"
            val invalids = result.evaluations
                .filter { !it.validJson }
                .sortedBy { it.id }

            if (invalids.isEmpty()) {
                lines += "No invalid JSON samples."
            } else {
                lines += "| Prompt | First Error | Retry Error | First Response | Retry Response |"
                lines += "|---|---|---|---|---|"
                invalids.forEach { evaluation ->
                    lines += "| ${mdCell(evaluation.id)} | ${mdCell(evaluation.firstError.orEmpty(), maxLen = 90)} | ${mdCell(evaluation.retryError.orEmpty(), maxLen = 90)} | ${mdCell(evaluation.firstResponse.orEmpty(), maxLen = 160)} | ${mdCell(evaluation.retryResponse.orEmpty(), maxLen = 160)} |"
                }
            }

            lines += ""
            lines += "### Latency By Prompt Type"
            lines += "| Type | Mean (ms) | P50 (ms) | P95 (ms) |"
            lines += "|---|---:|---:|---:|"
            lines += "| task | ${result.taskLatency.meanMs} | ${result.taskLatency.p50Ms} | ${result.taskLatency.p95Ms} |"
            lines += "| note | ${result.noteLatency.meanMs} | ${result.noteLatency.p50Ms} | ${result.noteLatency.p95Ms} |"

            lines += ""
            lines += "### Latency By Prompt Length"
            if (result.latencyByPromptLength.isEmpty()) {
                lines += "No prompt-length metrics."
            } else {
                lines += "| Bucket | Prompts | Mean (ms) | P50 (ms) | P95 (ms) | Fallback | Timeout | Utility |"
                lines += "|---|---:|---:|---:|---:|---:|---:|---:|"
                result.latencyByPromptLength.forEach { bucket ->
                    lines += "| ${bucket.bucket} | ${bucket.prompts} | ${bucket.meanMs} | ${bucket.p50Ms} | ${bucket.p95Ms} | ${bucket.fallbackRate} | ${bucket.timeoutRate} | ${bucket.utilityScore} |"
                }
            }

            val routing = report.routingRecommendations.firstOrNull { it.model == result.model }
            if (routing != null) {
                lines += ""
                lines += "### Routing Recommendation"
                lines += routing.summary
                lines += ""
                lines += "| Segment | Route | Prompts | Utility Delta | LLM P50 (ms) | Rule P50 (ms) | LLM Fallback | LLM Timeout | Reason |"
                lines += "|---|---|---:|---:|---:|---:|---:|---:|---|"
                routing.segments.forEach { segment ->
                    lines += "| ${segment.segment} | ${segment.routeTo} | ${segment.prompts} | ${segment.utilityDelta} | ${segment.llmP50LatencyMs} | ${segment.ruleP50LatencyMs} | ${segment.llmFallbackRate} | ${segment.llmTimeoutRate} | ${mdCell(segment.reason, maxLen = 120)} |"
                }
            }
        }

        return lines.joinToString("\n")
    }

    fun toRoutingMarkdown(report: BenchmarkSuiteReport): String {
        val lines = mutableListOf<String>()
        lines += "# LocalMind Routing Recommendations"
        lines += ""
        lines += "- Generated: ${report.generatedAt}"
        lines += "- Baseline: ${report.baselineModel}"
        lines += ""

        if (report.routingRecommendations.isEmpty()) {
            lines += "No routing recommendations generated."
            return lines.joinToString("\n")
        }

        report.routingRecommendations
            .sortedBy { it.model }
            .forEach { recommendation ->
                lines += "## Model: ${recommendation.model}"
                lines += ""
                lines += "- Strategy: ${recommendation.strategy}"
                lines += "- LLM coverage: ${recommendation.llmCoverageRate}"
                lines += "- Utility delta vs rule: ${recommendation.utilityDeltaVsRule}"
                lines += "- Mean latency delta vs rule (ms): ${recommendation.latencyDeltaVsRuleMs}"
                lines += "- Thresholds: minUtilityGain=${recommendation.thresholds.minUtilityGain}, maxFallbackRate=${recommendation.thresholds.maxFallbackRate}, maxTimeoutRate=${recommendation.thresholds.maxTimeoutRate}, maxP50LatencyMs=${recommendation.thresholds.maxP50LatencyMs}"
                lines += ""
                lines += recommendation.summary
                lines += ""
                lines += "| Segment | Route | Prompts | LLM Utility | Rule Utility | Delta | LLM Mean (ms) | Rule Mean (ms) | LLM Fallback | LLM Timeout | Reason |"
                lines += "|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---|"
                recommendation.segments.forEach { segment ->
                    lines += "| ${segment.segment} | ${segment.routeTo} | ${segment.prompts} | ${segment.llmUtility} | ${segment.ruleUtility} | ${segment.utilityDelta} | ${segment.llmMeanLatencyMs} | ${segment.ruleMeanLatencyMs} | ${segment.llmFallbackRate} | ${segment.llmTimeoutRate} | ${mdCell(segment.reason, maxLen = 120)} |"
                }
                lines += ""
            }

        return lines.joinToString("\n")
    }

    private fun mdCell(value: String, maxLen: Int = 200): String {
        if (value.isBlank()) return ""
        val singleLine = value.replace("\n", " ").replace("\r", " ").replace("|", "\\|").trim()
        return if (singleLine.length <= maxLen) singleLine else singleLine.take(maxLen - 3) + "..."
    }
}
