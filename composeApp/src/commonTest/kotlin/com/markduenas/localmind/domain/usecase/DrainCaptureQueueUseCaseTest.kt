package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.ai.RuleBasedParser
import com.markduenas.localmind.data.repository.FakeCaptureRepository
import com.markduenas.localmind.data.repository.FakeNoteRepository
import com.markduenas.localmind.data.repository.FakeTaskRepository
import com.markduenas.localmind.domain.model.Capture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlinx.coroutines.runBlocking

class DrainCaptureQueueUseCaseTest {

    private val taskRepository = FakeTaskRepository()
    private val noteRepository = FakeNoteRepository()
    private val captureRepository = FakeCaptureRepository()
    private val processCaptureUseCase = ProcessCaptureUseCase(
        ruleBasedParser = RuleBasedParser(),
        createNoteUseCase = CreateNoteUseCase(noteRepository),
        createTaskUseCase = CreateTaskUseCase(taskRepository),
        captureRepository = captureRepository,
    )
    private val useCase = DrainCaptureQueueUseCase(captureRepository, processCaptureUseCase)

    @Test
    fun processesAllPendingCapturesLeftFromPreviousSession() = runBlocking {
        captureRepository.save(
            Capture("1", "buy milk tomorrow", null, Clock.System.now(), processed = false)
        )
        captureRepository.save(
            Capture("2", "interesting article about sleep", null, Clock.System.now(), processed = false)
        )

        useCase()

        assertEquals(1, taskRepository.createdTasks.size)
        assertEquals(1, noteRepository.createdNotes.size)
        assertEquals(setOf("1", "2"), captureRepository.markedProcessedIds.toSet())
    }

    @Test
    fun noPendingCapturesIsANoOp() = runBlocking {
        useCase()

        assertEquals(0, taskRepository.createdTasks.size)
        assertEquals(0, noteRepository.createdNotes.size)
    }
}
