package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.data.repository.TaskRepository
import com.markduenas.localmind.domain.model.Task
import kotlinx.coroutines.flow.Flow

class GetTodayTasksUseCase(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return taskRepository.getTodayTasks()
    }
}
