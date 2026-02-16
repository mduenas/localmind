package com.markduenas.localmind.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.markduenas.localmind.data.local.LocalMindDb
import com.markduenas.localmind.data.local.toDomainTask
import com.markduenas.localmind.data.local.toEpochDaysLong
import com.markduenas.localmind.data.local.toSecondOfDayLong
import com.markduenas.localmind.domain.model.Task
import com.markduenas.localmind.domain.model.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class TaskRepositoryImpl(
    private val db: LocalMindDb
) : TaskRepository {

    private val queries get() = db.localMindDbQueries

    override fun getTodayTasks(): Flow<List<Task>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val todayEpoch = today.toEpochDaysLong()
        return queries.getTodayTasks(todayEpoch, todayEpoch)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks -> tasks.map { it.toDomainTask() } }
    }

    override fun getUpcomingTasks(days: Int): Flow<List<Task>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val todayEpoch = today.toEpochDaysLong()
        val endDate = LocalDate.fromEpochDays(today.toEpochDays() + days)
        val endEpoch = endDate.toEpochDaysLong()
        return queries.getUpcomingTasks(todayEpoch, endEpoch)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks -> tasks.map { it.toDomainTask() } }
    }

    override fun getAllTasks(): Flow<List<Task>> {
        return queries.getAllActiveTasks()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks -> tasks.map { it.toDomainTask() } }
    }

    override fun getTaskById(id: String): Flow<Task?> {
        return queries.getTaskById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomainTask() }
    }

    override suspend fun createTask(task: Task) {
        queries.insertTask(
            id = task.id,
            title = task.title,
            original_text = task.originalText,
            due_date = task.dueDate?.toEpochDaysLong(),
            due_time = task.dueTime?.toSecondOfDayLong(),
            priority = task.priority.value.toLong(),
            status = task.status.value.toLong(),
            created_at = task.createdAt.toEpochMilliseconds(),
            updated_at = task.updatedAt.toEpochMilliseconds(),
            parsing_confidence = task.parsingConfidence?.toDouble()
        )
    }

    override suspend fun updateTask(task: Task) {
        queries.updateTask(
            title = task.title,
            due_date = task.dueDate?.toEpochDaysLong(),
            due_time = task.dueTime?.toSecondOfDayLong(),
            priority = task.priority.value.toLong(),
            updated_at = Clock.System.now().toEpochMilliseconds(),
            id = task.id
        )
    }

    override suspend fun updateTaskStatus(id: String, status: TaskStatus) {
        val now = Clock.System.now().toEpochMilliseconds()
        val completedAt = if (status == TaskStatus.COMPLETED) now else null
        queries.updateTaskStatus(
            status = status.value.toLong(),
            updated_at = now,
            completed_at = completedAt,
            id = id
        )
    }

    override suspend fun deleteTask(id: String) {
        queries.deleteTask(id)
    }

    override fun searchTasks(query: String): Flow<List<Task>> {
        val searchQuery = "%$query%"
        return queries.searchTasks(searchQuery, searchQuery)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { tasks -> tasks.map { it.toDomainTask() } }
    }
}
