package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.data.repository.FakeTaskRepository
import com.markduenas.localmind.domain.model.ParsedTask
import com.markduenas.localmind.domain.model.Priority
import com.markduenas.localmind.domain.model.TaskStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate

class CreateTaskUseCaseTest {

    @Test
    fun createsTaskWithCorrectFields(): Unit = runBlocking {
        val repository = FakeTaskRepository()
        val useCase = CreateTaskUseCase(repository)

        val parsedTask = ParsedTask(
            title = "Buy groceries",
            dueDate = LocalDate(2026, 3, 1),
            dueTime = null,
            priority = Priority.HIGH,
            tags = listOf("shopping"),
            originalText = "buy groceries march 1 #shopping",
            confidence = 0.85f,
            suggestedEdits = null,
        )

        val task = useCase(parsedTask)

        assertEquals("Buy groceries", task.title)
        assertEquals(LocalDate(2026, 3, 1), task.dueDate)
        assertEquals(Priority.HIGH, task.priority)
        assertEquals(TaskStatus.PENDING, task.status)
        assertEquals("buy groceries march 1 #shopping", task.originalText)
        assertEquals(0.85f, task.parsingConfidence)
        assertNotNull(task.id)
        assertTrue(task.id.isNotBlank())
        assertNotNull(task.createdAt)
        assertNotNull(task.updatedAt)
    }

    @Test
    fun repositoryCreateTaskCalled(): Unit = runBlocking {
        val repository = FakeTaskRepository()
        val useCase = CreateTaskUseCase(repository)

        val parsedTask = ParsedTask(
            title = "Test task",
            dueDate = null,
            dueTime = null,
            priority = Priority.MEDIUM,
            tags = emptyList(),
            originalText = "test task",
            confidence = 0.7f,
            suggestedEdits = null,
        )

        useCase(parsedTask)

        assertEquals(1, repository.createdTasks.size)
        assertEquals("Test task", repository.createdTasks[0].title)
    }
}
