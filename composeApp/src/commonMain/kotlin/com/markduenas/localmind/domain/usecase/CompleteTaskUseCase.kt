package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.data.repository.TaskRepository
import com.markduenas.localmind.domain.model.TaskStatus

class CompleteTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String) {
        taskRepository.updateTaskStatus(taskId, TaskStatus.COMPLETED)
    }
}
