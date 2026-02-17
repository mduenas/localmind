package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.ai.RuleBasedParser
import com.markduenas.localmind.domain.model.ParseResult
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
        assertEquals("Buy groceries", result.task.title)
    }

    @Test
    fun llmEnabledAndSuccessUsesTaskParser() = runBlocking {
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
        val useCase = ParseCaptureUseCase(
            taskParser = StubTaskParser(result = parsed),
            ruleBasedParser = ruleBasedParser,
            isLLMEnabled = { true },
        )

        val result = useCase("buy groceries")

        assertIs<ParseResult.Success>(result)
        assertEquals(0.95f, result.task.confidence)
    }

    @Test
    fun llmEnabledButThrowsFallsBackToRuleBased() = runBlocking {
        val useCase = ParseCaptureUseCase(
            taskParser = StubTaskParser(shouldThrow = true),
            ruleBasedParser = ruleBasedParser,
            isLLMEnabled = { true },
        )

        val result = useCase("buy groceries tomorrow")

        assertIs<ParseResult.Fallback>(result)
        assertEquals("Buy groceries", result.task.title)
        assertTrue(result.reason != null)
    }
}
