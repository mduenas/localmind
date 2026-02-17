package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.data.repository.FakeTaskRepository
import com.markduenas.localmind.domain.model.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class CompleteTaskUseCaseTest {

    @Test
    fun callsUpdateTaskStatusWithCompleted() = runBlocking {
        val repository = FakeTaskRepository()
        val useCase = CompleteTaskUseCase(repository)

        useCase("task-123")

        assertEquals(1, repository.statusUpdates.size)
        assertEquals("task-123", repository.statusUpdates[0].first)
        assertEquals(TaskStatus.COMPLETED, repository.statusUpdates[0].second)
    }

    @Test
    fun passesCorrectTaskId() = runBlocking {
        val repository = FakeTaskRepository()
        val useCase = CompleteTaskUseCase(repository)

        useCase("abc-def-ghi")

        assertEquals("abc-def-ghi", repository.statusUpdates[0].first)
    }
}
