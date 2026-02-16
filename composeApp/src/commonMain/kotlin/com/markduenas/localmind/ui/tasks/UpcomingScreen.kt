package com.markduenas.localmind.ui.tasks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markduenas.localmind.domain.model.Task
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UpcomingScreen(
    viewModel: TaskListViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.upcomingTasks.isEmpty()) {
        EmptyState(
            title = "No upcoming tasks",
            subtitle = "Tasks due in the next 7 days will appear here",
        )
        return
    }

    val grouped: Map<LocalDate?, List<Task>> = state.upcomingTasks.groupBy { it.dueDate }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = 8.dp),
    ) {
        grouped.forEach { (date, tasks) ->
            item {
                val sectionTitle = date?.let {
                    "${it.month.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }} ${it.dayOfMonth}"
                } ?: "No date"
                TaskSection(
                    title = sectionTitle,
                    tasks = tasks,
                    onToggleComplete = viewModel::toggleComplete,
                )
            }
        }
    }
}
