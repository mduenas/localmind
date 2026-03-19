package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.ai.RuleBasedParser
import com.markduenas.localmind.domain.model.ParseResult
import com.markduenas.localmind.domain.model.ParsedCapture
import com.markduenas.localmind.domain.model.ParsedTask
import com.markduenas.localmind.domain.model.Priority
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class ParseCaptureUseCaseTest {

    private val ruleBasedParser = RuleBasedParser()

    @Test
    fun blankInputReturnsError() = runBlocking {
        val useCase = ParseCaptureUseCase(
            taskParser = StubTaskParser(),
            ruleBasedParser = ruleBasedParser,
            isLLMEnabled = { false },
        )

        val result = useCase("   ")

        assertIs<ParseResult.Error>(result)
        assertEquals("Input text is empty", result.message)
    }

    @Test
    fun llmDisabledUsesRuleBasedParser() = runBlocking {
        val useCase = ParseCaptureUseCase(
            taskParser = StubTaskParser(),
            ruleBasedParser = ruleBasedParser,
            isLLMEnabled = { false },
        )

        val result = useCase("buy groceries tomorrow")

        assertIs<ParseResult.Success>(result)
        val capture = result.capture
        assertIs<ParsedCapture.TaskCapture>(capture)
        assertEquals("Buy groceries", capture.task.title)
    }

    @Test
    fun llmEnabledAndPremiumUsesTaskParser() = runBlocking {
        val parsed = ParsedTask(
            title = "Buy groceries",
            dueDate = null,
            dueTime = null,
            priority = Priority.MEDIUM,
            tags = emptyList(),
            originalText = "buy groceries",
            confidence = 0.95f,
            suggestedEdits = null,
        )
        val taskParser = StubTaskParser(result = parsed)
        val useCase = ParseCaptureUseCase(
            taskParser = taskParser,
            ruleBasedParser = ruleBasedParser,
            isLLMEnabled = { true },
            isPremium = { true },
        )

        val result = useCase("buy groceries for family this weekend and schedule pickup with neighbor")

        assertIs<ParseResult.Success>(result)
        val capture = result.capture
        assertIs<ParsedCapture.TaskCapture>(capture)
        assertEquals(0.95f, capture.task.confidence)
        assertEquals(1, taskParser.parseCallCount)
    }

    @Test
    fun llmEnabledButNotPremiumUsesRuleBased() = runBlocking {
        val useCase = ParseCaptureUseCase(
            taskParser = StubTaskParser(shouldThrow = true),
            ruleBasedParser = ruleBasedParser,
            isLLMEnabled = { true },
            isPremium = { false },
        )

        val result = useCase("buy groceries tomorrow")

        assertIs<ParseResult.Success>(result)
        val capture = result.capture
        assertIs<ParsedCapture.TaskCapture>(capture)
        assertEquals("Buy groceries", capture.task.title)
    }

    @Test
    fun llmEnabledAndPremiumButThrowsFallsBackToRuleBased() = runBlocking {
        val taskParser = StubTaskParser(shouldThrow = true)
        val useCase = ParseCaptureUseCase(
            taskParser = taskParser,
            ruleBasedParser = ruleBasedParser,
            isLLMEnabled = { true },
            isPremium = { true },
        )

        val result = useCase("buy groceries tomorrow and call the contractor before lunch")

        assertIs<ParseResult.Fallback>(result)
        val capture = result.capture
        assertIs<ParsedCapture.TaskCapture>(capture)
        assertTrue(capture.task.title.contains("Buy groceries", ignoreCase = true))
        assertTrue(result.reason != null)
        assertEquals(1, taskParser.parseCallCount)
    }

    @Test
    fun llmEnabledAndPremiumSimplePromptUsesFastRulePath() = runBlocking {
        val taskParser = StubTaskParser(shouldThrow = true)
        val useCase = ParseCaptureUseCase(
            taskParser = taskParser,
            ruleBasedParser = ruleBasedParser,
            isLLMEnabled = { true },
            isPremium = { true },
        )

        val result = useCase("get milk tomorrow")

        assertIs<ParseResult.Success>(result)
        val capture = result.capture
        assertIs<ParsedCapture.TaskCapture>(capture)
        assertEquals("Get milk", capture.task.title)
        assertEquals(0, taskParser.parseCallCount)
    }
}
