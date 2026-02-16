package com.markduenas.localmind.ui.tasks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AllTasksScreen(
    viewModel: TaskListViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.allTasks.isEmpty()) {
        EmptyState(
            title = "No tasks yet",
            subtitle = "Capture your first task to get started",
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = 8.dp),
    ) {
        item {
            TaskSection(
                title = "All Tasks",
                tasks = state.allTasks,
                onToggleComplete = viewModel::toggleComplete,
            )
        }
    }
}
