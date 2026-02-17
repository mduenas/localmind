package com.markduenas.localmind.data.repository

import com.markduenas.localmind.domain.model.Task
import com.markduenas.localmind.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTaskRepository : TaskRepository {

    val createdTasks = mutableListOf<Task>()
    val statusUpdates = mutableListOf<Pair<String, TaskStatus>>()
    val deletedTaskIds = mutableListOf<String>()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())

    override fun getTodayTasks(): Flow<List<Task>> = _tasks
    override fun getUpcomingTasks(days: Int): Flow<List<Task>> = _tasks
    override fun getAllTasks(): Flow<List<Task>> = _tasks
    override fun getTaskById(id: String): Flow<Task?> = MutableStateFlow(
        _tasks.value.find { it.id == id }
    )

    override suspend fun createTask(task: Task) {
        createdTasks.add(task)
        _tasks.value = _tasks.value + task
    }

    override suspend fun updateTask(task: Task) {
        _tasks.value = _tasks.value.map { if (it.id == task.id) task else it }
    }

    override suspend fun updateTaskStatus(id: String, status: TaskStatus) {
        statusUpdates.add(id to status)
    }

    override suspend fun deleteTask(id: String) {
        deletedTaskIds.add(id)
        _tasks.value = _tasks.value.filter { it.id != id }
    }

    override fun searchTasks(query: String): Flow<List<Task>> = _tasks
}
