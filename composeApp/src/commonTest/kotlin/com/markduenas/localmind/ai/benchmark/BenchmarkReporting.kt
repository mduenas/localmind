package com.markduenas.localmind.ai.benchmark

import com.markduenas.localmind.ai.AIConfig
import com.markduenas.localmind.domain.model.ParsedCapture
import kotlin.math.roundToInt
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
        "gemma3-1b",
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
        ModelCatalogEntry(slug = "gemma3-1b", approxSizeMb = 700, source = "cactus"),
        ModelCatalogEntry(slug = "qwen3-1.7", approxSizeMb = 1100, source = "cactus"),
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

/**
 * Prints live per-prompt output and aggregate summaries to stdout during a benchmark run.
 *
 * Each prompt prints two lines:
 *   task_01   ✓   2ms  TASK  "Call mom"  date=2026-05-04 time=18:00 pri=MEDIUM tags=[]
 *                       ← "remind me to call mom before dinner at 6pm"
 *
 * Failures show an extra hint line explaining what was wrong.
 */
object BenchmarkLiveLogger {

    private val LINE = "─".repeat(80)
    private val BOLD_LINE = "━".repeat(80)

    fun header(model: String, suiteVersion: String, promptCount: Int) {
        println()
        println(BOLD_LINE)
        println("  BENCHMARK  model=$model  suite=$suiteVersion  prompts=$promptCount")
        println(BOLD_LINE)
    }

    /**
     * Prints a compact result row for one evaluated prompt.
     * Call this immediately after [BenchmarkScorer.scorePrompt] so output streams live.
     */
    fun prompt(
        fixture: BenchmarkPromptFixture,
        eval: PromptEvaluation,
        capture: ParsedCapture,
    ) {
        val pass = eval.classificationCorrect && eval.fieldScore >= 1.0
        val icon = if (pass) "✓" else "✗"
        val ms = "${eval.latencyMs}ms".padStart(6)

        val outputDesc = when (capture) {
            is ParsedCapture.TaskCapture -> {
                val t = capture.task
                val date = t.dueDate?.toString() ?: "null"
                val time = t.dueTime?.toString() ?: "null"
                val tags = if (t.tags.isEmpty()) "" else "  tags=${t.tags}"
                "TASK  \"${t.title.take(42)}\"  date=$date  time=$time  pri=${t.priority.name}$tags"
            }
            is ParsedCapture.NoteCapture -> {
                val n = capture.note
                "NOTE  \"${n.title.take(60)}\""
            }
        }

        val idCol = fixture.id.padEnd(10)
        println("$idCol $icon  $ms  $outputDesc")
        println("${"".padEnd(10)}          ← \"${fixture.prompt.take(75)}\"")

        if (!pass) {
            val hints = mutableListOf<String>()
            if (!eval.classificationCorrect) {
                hints += "expected=${eval.expectedType}, got=${eval.actualType}"
            } else {
                val score = (eval.fieldScore * 100).roundToInt()
                hints += "fieldScore=$score%"
                if (eval.fallbackUsed) hints += "fallback"
            }
            println("${"".padEnd(10)}          ✗ ${hints.joinToString("  ")}")
        }
    }

    fun summary(result: ModelBenchmarkResult) {
        val total = result.promptsEvaluated
        val correct = (result.classificationAccuracy * total).roundToInt()
        println()
        println(BOLD_LINE)
        println("  RESULTS  model=${result.model}  prompts=$total")
        println(LINE)
        println("  Classification:   ${pct(result.classificationAccuracy)}  ($correct/$total correct)")
        println("  Task fields:      ${pct(result.taskFieldAccuracy)}")
        println("  Note fields:      ${pct(result.noteFieldAccuracy)}")
        println("  Composite score:  ${result.compositeScore}")
        if (result.fallbackRate > 0.0) {
            println("  Fallback rate:    ${pct(result.fallbackRate)}")
        }
        if (result.timeoutRate > 0.0) {
            println("  Timeout rate:     ${pct(result.timeoutRate)}")
        }
        println(LINE)
        println("  Latency (all):    mean=${result.latency.meanMs}ms  p50=${result.latency.p50Ms}ms  p95=${result.latency.p95Ms}ms")
        println("  Latency (tasks):  mean=${result.taskLatency.meanMs}ms  p50=${result.taskLatency.p50Ms}ms  p95=${result.taskLatency.p95Ms}ms")
        println("  Latency (notes):  mean=${result.noteLatency.meanMs}ms  p50=${result.noteLatency.p50Ms}ms  p95=${result.noteLatency.p95Ms}ms")
        println(BOLD_LINE)
    }

    private fun pct(v: Double) = "${(v * 100).roundToInt()}%"
}
