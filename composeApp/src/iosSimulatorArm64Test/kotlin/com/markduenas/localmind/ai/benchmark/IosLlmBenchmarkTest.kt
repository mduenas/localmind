package com.markduenas.localmind.ai.benchmark

import com.cactus.ChatMessage
import com.cactus.CactusCompletionParams
import com.cactus.CactusInitParams
import com.cactus.CactusLM
import com.markduenas.localmind.ai.AIConfig
import com.markduenas.localmind.ai.JsonParser
import com.markduenas.localmind.ai.ModelManager
import com.markduenas.localmind.ai.Prompts
import com.markduenas.localmind.ai.RuleBasedParser
import com.markduenas.localmind.ai.directorySize
import com.markduenas.localmind.domain.model.ParsedCapture
import kotlin.test.Test
import kotlin.time.Clock
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import platform.Foundation.NSProcessInfo

class IosLlmBenchmarkTest {

    @Test
    fun runBenchmarkSuite() = runBlocking {
        if (!shouldRunBenchmark()) {
            println("Skipping local LLM benchmark. Set LOCALMIND_RUN_LLM_BENCHMARK=true to run.")
            return@runBlocking
        }

        val fixtureSuite = BenchmarkFixtures.suite
        val modelManager = ModelManager()
        val ruleParser = RuleBasedParser()

        val llmModels = resolveBenchmarkModels(modelManager)
        val baseline = runRuleBaseline(fixtureSuite.prompts, ruleParser)
        val llmResults = llmModels.mapNotNull { model ->
            runCatching {
                runLlmBenchmarkForModel(
                    modelSlug = model,
                    prompts = fixtureSuite.prompts,
                    modelManager = modelManager,
                    ruleParser = ruleParser,
                )
            }.onFailure { error ->
                println("Skipping model '$model' due to benchmark failure: ${error.message}")
            }.getOrNull()
        }

        if (llmResults.isEmpty()) {
            println("No LLM models could be benchmarked successfully; report will include baseline only.")
        }

        val benchmarkedSet = llmResults.map { it.model }.toSet()
        val installed = modelManager.getDownloadedModels().toSet()
        val suggestions = BenchmarkModelCatalog.suggestModels(
            installedModels = installed,
            benchmarkedModels = benchmarkedSet,
        )
        val routingRecommendations = llmResults.map { llmResult ->
            BenchmarkRoutingAdvisor.recommend(
                baseline = baseline,
                llmResult = llmResult,
            )
        }

        val report = BenchmarkSuiteReport(
            generatedAt = BenchmarkReportRenderer.nowIsoString(),
            suiteVersion = fixtureSuite.suiteVersion,
            benchmarkedModels = llmResults.map { it.model },
            baselineModel = baseline.model,
            results = listOf(baseline) + llmResults,
            suggestions = suggestions,
            routingRecommendations = routingRecommendations,
        )

        emitBlock("JSON", BenchmarkReportRenderer.toJson(report))
        emitBlock("SUMMARY_MD", BenchmarkReportRenderer.toSummaryMarkdown(report))
        emitBlock("SUGGESTIONS_MD", BenchmarkReportRenderer.toSuggestionsMarkdown(report))
        emitBlock("DETAILED_MD", BenchmarkReportRenderer.toDetailedMarkdown(report))
        emitBlock("ROUTING_MD", BenchmarkReportRenderer.toRoutingMarkdown(report))
    }

    private fun shouldRunBenchmark(): Boolean {
        val value = envOrArg("LOCALMIND_RUN_LLM_BENCHMARK")?.trim()?.lowercase() ?: "false"
        return value == "1" || value == "true" || value == "yes"
    }

    private suspend fun resolveBenchmarkModels(modelManager: ModelManager): List<String> {
        val primary = BenchmarkModelCatalog.primaryCandidates

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("  MODEL SETUP")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("  Primary models: ${primary.joinToString(", ")}")

        val forcedThird = envOrArg("LOCALMIND_BENCH_THIRD_MODEL")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.takeIf { !primary.contains(it) }

        val third = forcedThird
            ?: selectInstalledThirdCandidate(modelManager, primary)
            ?: probeThirdModelCandidate(modelManager, primary)

        if (third == null) {
            println("  Third model: none resolved")
        } else {
            println("  Third model: $third")
        }
        println()

        return if (third != null) primary + third else primary
    }

    private fun selectInstalledThirdCandidate(modelManager: ModelManager, primary: List<String>): String? {
        return modelManager.getDownloadedModels()
            .asSequence()
            .filterNot { primary.contains(it) }
            .sorted()
            .firstOrNull { isLikelySmallModel(it) }
    }

    private suspend fun probeThirdModelCandidate(modelManager: ModelManager, primary: List<String>): String? {
        for (candidate in BenchmarkModelCatalog.thirdModelProbeCandidates) {
            if (primary.contains(candidate)) continue

            val cacheAttempt = runCatching { ensureModelCached(modelManager, candidate) }
            if (cacheAttempt.isFailure && !modelManager.isModelDownloaded(candidate)) {
                println("Probe candidate '$candidate' unavailable: ${cacheAttempt.exceptionOrNull()?.message}")
                continue
            }

            val lm = CactusLM()
            val worked = runCatching {
                lm.initializeModel(CactusInitParams(model = candidate, contextSize = AIConfig.CONTEXT_SIZE))
                lm.unload()
            }.isSuccess

            if (worked) {
                return candidate
            }
        }
        return null
    }

    private fun isLikelySmallModel(slug: String): Boolean {
        val lower = slug.lowercase()
        return lower.contains("270m") ||
            lower.contains("0.5") ||
            lower.contains("0.6") ||
            lower.contains("1b") ||
            lower.contains("1.5b")
    }

    private suspend fun runRuleBaseline(
        prompts: List<BenchmarkPromptFixture>,
        ruleParser: RuleBasedParser,
    ): ModelBenchmarkResult {
        val suiteVersion = BenchmarkFixtures.suite.suiteVersion
        BenchmarkLiveLogger.header("rule-based", suiteVersion, prompts.size)

        val evaluations = prompts.map { fixture ->
            val started = Clock.System.now()
            val parsed = ruleParser.parse(fixture.prompt)
            val latencyMs = (Clock.System.now() - started).inWholeMilliseconds
            val eval = BenchmarkScorer.scorePrompt(
                fixture = fixture,
                capture = parsed,
                latencyMs = latencyMs,
                validJson = true,
                fallbackUsed = false,
            )
            BenchmarkLiveLogger.prompt(fixture, eval, parsed)
            eval
        }

        val result = BenchmarkScorer.aggregate(
            model = "rule-based",
            cacheHit = true,
            evaluations = evaluations,
        )
        BenchmarkLiveLogger.summary(result)
        return result
    }

    private suspend fun runLlmBenchmarkForModel(
        modelSlug: String,
        prompts: List<BenchmarkPromptFixture>,
        modelManager: ModelManager,
        ruleParser: RuleBasedParser,
    ): ModelBenchmarkResult {
        val cacheHit = ensureModelCached(modelManager, modelSlug)
        val lm = CactusLM()

        try {
            lm.initializeModel(
                CactusInitParams(
                    model = modelSlug,
                    contextSize = AIConfig.CONTEXT_SIZE,
                )
            )
            warmup(lm, modelSlug)

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
            BenchmarkLiveLogger.header(modelSlug, BenchmarkFixtures.suite.suiteVersion, prompts.size)

            val evaluations = prompts.map { fixture ->
                val systemPrompt = Prompts.systemPromptForModel(modelSlug)
                val userPrompt = Prompts.buildUserPrompt(fixture.prompt, today, modelSlug)
                val started = Clock.System.now()
                val maxTokens = maxTokensForInput(fixture.prompt)

                var firstError: String? = null
                var firstResponse: String? = null

                val firstResponseAttempt = runCatching {
                    withTimeout(AIConfig.timeoutMsForModel(modelSlug)) {
                        val messages = buildList {
                            if (systemPrompt != null) add(ChatMessage(content = systemPrompt, role = "system"))
                            add(ChatMessage(content = userPrompt, role = "user"))
                        }
                        lm.generateCompletion(
                            messages = messages,
                            params = CactusCompletionParams(
                                maxTokens = maxTokens,
                                temperature = AIConfig.TEMPERATURE,
                            ),
                        )
                    }
                }.mapCatching { result ->
                    if (result == null || !result.success) {
                        error("Model generation failed")
                    }
                    result.response ?: error("Model response was empty")
                }.onSuccess {
                    firstResponse = it
                }.onFailure {
                    firstError = it.message
                }

                val firstCaptureAttempt = firstResponseAttempt
                    .mapCatching { response ->
                        JsonParser.parse(response, fixture.prompt)
                    }

                if (firstCaptureAttempt.isFailure && firstError == null) {
                    firstError = firstCaptureAttempt.exceptionOrNull()?.message
                }

                val captureAttempt = firstCaptureAttempt

                val latencyMs = (Clock.System.now() - started).inWholeMilliseconds

                val scoredCapture: ParsedCapture
                val validJson: Boolean
                val fallbackUsed: Boolean
                val error: String?

                if (captureAttempt.isSuccess) {
                    scoredCapture = captureAttempt.getOrThrow()
                    validJson = true
                    fallbackUsed = false
                    error = null
                } else {
                    scoredCapture = ruleParser.parse(fixture.prompt)
                    validJson = false
                    fallbackUsed = true
                    error = captureAttempt.exceptionOrNull()?.message
                }

                BenchmarkScorer.scorePrompt(
                    fixture = fixture,
                    capture = scoredCapture,
                    latencyMs = latencyMs,
                    validJson = validJson,
                    fallbackUsed = fallbackUsed,
                    error = error,
                    firstError = firstError,
                    firstResponse = firstResponse,
                ).also { eval ->
                    BenchmarkLiveLogger.prompt(fixture, eval, scoredCapture)
                }
            }

            val result = BenchmarkScorer.aggregate(
                model = modelSlug,
                cacheHit = cacheHit,
                evaluations = evaluations,
            )
            BenchmarkLiveLogger.summary(result)
            return result
        } finally {
            lm.unload()
        }
    }

    private suspend fun ensureModelCached(modelManager: ModelManager, model: String): Boolean {
        val modelsDir = modelManager.getModelsDirectory()
        println("  ? Checking $model at $modelsDir/$model ...")
        if (modelManager.isModelDownloaded(model)) {
            println("  ✓ $model — already downloaded")
            return true
        }

        val sizeMb = AIConfig.MODEL_SIZES[model] ?: "unknown size"
        val expectedBytes = AIConfig.MODEL_BYTES[model] ?: 0L

        println("  ⬇ $model ($sizeMb) — downloading...")

        val downloadTimeoutMs = 15 * 60_000L // 15 minutes
        try {
            withTimeout(downloadTimeoutMs) {
                coroutineScope {
                    val downloadJob = launch { modelManager.downloadModel(model) }
                    // Poll directory size for live progress while download runs
                    launch {
                        while (downloadJob.isActive) {
                            delay(4_000)
                            if (!downloadJob.isActive) break
                            val got = directorySize(modelsDir)
                            if (expectedBytes > 0) {
                                val pct = (got * 100L / expectedBytes).coerceIn(0, 100)
                                val gotMb = got / 1_000_000
                                val totalMb = expectedBytes / 1_000_000
                                println("    ... ${gotMb}MB / ${totalMb}MB ($pct%)")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to download model '$model': ${e.message}", e)
        }

        println("  ✓ $model — download complete")
        return false
    }

    private suspend fun warmup(lm: CactusLM, modelSlug: String) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val warmupSystem = Prompts.systemPromptForModel(modelSlug)
        val result = withTimeout(AIConfig.timeoutMsForModel(modelSlug)) {
            val messages = buildList {
                if (warmupSystem != null) add(ChatMessage(content = warmupSystem, role = "system"))
                add(ChatMessage(content = Prompts.buildUserPrompt("remind me to drink water", today, modelSlug), role = "user"))
            }
            lm.generateCompletion(
                messages = messages,
                params = CactusCompletionParams(
                    maxTokens = AIConfig.MAX_TOKENS_SHORT_INPUT,
                    temperature = AIConfig.TEMPERATURE,
                ),
            )
        }
        if (result == null || !result.success) {
            error("Warmup failed")
        }
    }

    private fun maxTokensForInput(rawText: String): Int {
        val len = rawText.trim().length
        return when {
            len <= 80 -> AIConfig.MAX_TOKENS_SHORT_INPUT
            len <= 200 -> AIConfig.MAX_TOKENS_MEDIUM_INPUT
            else -> AIConfig.MAX_TOKENS_LONG_INPUT
        }
    }

    private fun env(name: String): String? {
        return NSProcessInfo.processInfo.environment[name] as? String
    }

    private fun envOrArg(name: String): String? {
        env(name)?.let { return it }
        val prefix = "$name="
        val args = NSProcessInfo.processInfo.arguments
        for (rawArg in args) {
            val arg = rawArg.toString()
            if (arg.startsWith(prefix)) {
                return arg.removePrefix(prefix)
            }
        }
        return null
    }

    private fun emitBlock(name: String, body: String) {
        println("LOCALMIND_BENCH_${name}_BEGIN")
        println(body)
        println("LOCALMIND_BENCH_${name}_END")
    }
}
