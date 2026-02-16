package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.data.repository.TaskRepository
import com.markduenas.localmind.domain.model.Task
import kotlinx.coroutines.flow.Flow

class GetUpcomingTasksUseCase(
    private val taskRepository: TaskRepository,
) {
    operator fun invoke(days: Int = 7): Flow<List<Task>> {
        return taskRepository.getUpcomingTasks(days)
    }
}
