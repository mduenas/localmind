package com.markduenas.localmind.domain.usecase

import com.benasher44.uuid.uuid4
import com.markduenas.localmind.data.repository.TaskRepository
import com.markduenas.localmind.domain.model.ParsedTask
import com.markduenas.localmind.domain.model.Task
import com.markduenas.localmind.domain.model.TaskStatus
import kotlin.time.Clock

class CreateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(parsedTask: ParsedTask): Task {
        val now = Clock.System.now()
        val task = Task(
            id = uuid4().toString(),
            title = parsedTask.title,
            originalText = parsedTask.originalText,
            dueDate = parsedTask.dueDate,
            dueTime = parsedTask.dueTime,
            priority = parsedTask.priority,
            status = TaskStatus.PENDING,
            tags = emptyList(),
            createdAt = now,
            updatedAt = now,
            completedAt = null,
            parsingConfidence = parsedTask.confidence
        )
        taskRepository.createTask(task)
        return task
    }
}
