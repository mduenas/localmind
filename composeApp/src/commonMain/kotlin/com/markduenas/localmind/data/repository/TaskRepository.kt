package com.markduenas.localmind.data.repository

import com.markduenas.localmind.domain.model.Task
import com.markduenas.localmind.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface TaskRepository {
    fun getTodayTasks(): Flow<List<Task>>
    fun getUpcomingTasks(days: Int = 7): Flow<List<Task>>
    fun getTasksByDateRange(start: LocalDate, end: LocalDate): Flow<List<Task>>
    fun getAllTasks(): Flow<List<Task>>
    fun getTaskById(id: String): Flow<Task?>
    suspend fun createTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun updateTaskStatus(id: String, status: TaskStatus)
    suspend fun deleteTask(id: String)
    fun searchTasks(query: String): Flow<List<Task>>
}
