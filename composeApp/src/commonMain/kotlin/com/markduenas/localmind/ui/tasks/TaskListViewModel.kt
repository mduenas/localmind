package com.markduenas.localmind.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markduenas.localmind.data.repository.TaskRepository
import com.markduenas.localmind.domain.model.Task
import com.markduenas.localmind.domain.model.TaskStatus
import com.markduenas.localmind.domain.usecase.CompleteTaskUseCase
import com.markduenas.localmind.domain.usecase.GetTodayTasksUseCase
import com.markduenas.localmind.domain.usecase.GetUpcomingTasksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskListUiState(
    val todayTasks: List<Task> = emptyList(),
    val upcomingTasks: List<Task> = emptyList(),
    val allTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
)

class TaskListViewModel(
    private val getTodayTasksUseCase: GetTodayTasksUseCase,
    private val getUpcomingTasksUseCase: GetUpcomingTasksUseCase,
    private val taskRepository: TaskRepository,
    private val completeTaskUseCase: CompleteTaskUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            getTodayTasksUseCase()
                .catch { /* silently handle errors for now */ }
                .collect { tasks ->
                    _uiState.update { it.copy(todayTasks = tasks, isLoading = false) }
                }
        }
        viewModelScope.launch {
            getUpcomingTasksUseCase()
                .catch { }
                .collect { tasks ->
                    _uiState.update { it.copy(upcomingTasks = tasks) }
                }
        }
        viewModelScope.launch {
            taskRepository.getAllTasks()
                .catch { }
                .collect { tasks ->
                    _uiState.update { it.copy(allTasks = tasks) }
                }
        }
    }

    fun toggleComplete(taskId: String) {
        viewModelScope.launch {
            val task = _uiState.value.allTasks.find { it.id == taskId }
                ?: _uiState.value.todayTasks.find { it.id == taskId }
                ?: _uiState.value.upcomingTasks.find { it.id == taskId }
            if (task != null) {
                if (task.status == TaskStatus.COMPLETED) {
                    taskRepository.updateTaskStatus(taskId, TaskStatus.PENDING)
                } else {
                    completeTaskUseCase(taskId)
                }
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
        }
    }
}
