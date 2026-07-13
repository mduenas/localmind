package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.ai.RuleBasedParser
import com.markduenas.localmind.data.repository.FakeCaptureRepository
import com.markduenas.localmind.data.repository.FakeNoteRepository
import com.markduenas.localmind.data.repository.FakeTaskRepository
import com.markduenas.localmind.domain.model.Capture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.coroutines.runBlocking

class ProcessCaptureUseCaseTest {

    private val taskRepository = FakeTaskRepository()
    private val noteRepository = FakeNoteRepository()
    private val captureRepository = FakeCaptureRepository()
    private val useCase = ProcessCaptureUseCase(
        ruleBasedParser = RuleBasedParser(),
        createNoteUseCase = CreateNoteUseCase(noteRepository),
        createTaskUseCase = CreateTaskUseCase(taskRepository),
        captureRepository = captureRepository,
    )

    private fun capture(rawText: String) = Capture(
        id = "capture-1",
        rawText = rawText,
        audioPath = null,
        createdAt = Clock.System.now(),
        processed = false,
    )

    @Test
    fun taskLikeTextCreatesTaskAndMarksProcessed() = runBlocking {
        val capture = capture("buy milk tomorrow")

        useCase(capture)

        assertEquals(1, taskRepository.createdTasks.size)
        assertEquals(0, noteRepository.createdNotes.size)
        assertTrue(captureRepository.markedProcessedIds.contains(capture.id))
    }

    @Test
    fun noteLikeTextCreatesNoteAndMarksProcessed() = runBlocking {
        val capture = capture("interesting article about sleep")

        useCase(capture)

        assertEquals(1, noteRepository.createdNotes.size)
        assertEquals(0, taskRepository.createdTasks.size)
        assertTrue(captureRepository.markedProcessedIds.contains(capture.id))
    }

    @Test
    fun alreadyProcessedCaptureIsSkipped() = runBlocking {
        val capture = capture("buy milk tomorrow").copy(processed = true)

        useCase(capture)

        assertEquals(0, taskRepository.createdTasks.size)
        assertEquals(0, noteRepository.createdNotes.size)
        assertTrue(captureRepository.markedProcessedIds.isEmpty())
    }
}
