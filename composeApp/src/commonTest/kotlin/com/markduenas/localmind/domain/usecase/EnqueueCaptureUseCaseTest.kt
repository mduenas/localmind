package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.ai.RuleBasedParser
import com.markduenas.localmind.data.repository.FakeCaptureRepository
import com.markduenas.localmind.data.repository.FakeNoteRepository
import com.markduenas.localmind.data.repository.FakeTaskRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class EnqueueCaptureUseCaseTest {

    private val captureRepository = FakeCaptureRepository()
    private val processCaptureUseCase = ProcessCaptureUseCase(
        ruleBasedParser = RuleBasedParser(),
        createNoteUseCase = CreateNoteUseCase(FakeNoteRepository()),
        createTaskUseCase = CreateTaskUseCase(FakeTaskRepository()),
        captureRepository = captureRepository,
    )
    private val useCase = EnqueueCaptureUseCase(captureRepository, processCaptureUseCase)

    @Test
    fun blankTextIsRejected() = runBlocking {
        val id = useCase("   ")

        assertNull(id)
        assertTrue(captureRepository.savedCaptures.isEmpty())
    }

    @Test
    fun nonBlankTextIsSavedImmediatelyAsUnprocessed() = runBlocking {
        val id = useCase("buy milk tomorrow")

        assertTrue(id != null)
        assertEquals(1, captureRepository.savedCaptures.size)
        val saved = captureRepository.savedCaptures.first()
        assertEquals(id, saved.id)
        assertEquals("buy milk tomorrow", saved.rawText)
        assertEquals(false, saved.processed)
    }
}
