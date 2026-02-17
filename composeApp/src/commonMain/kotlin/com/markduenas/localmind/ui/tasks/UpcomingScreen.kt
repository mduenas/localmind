package com.markduenas.localmind.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    if (state.error != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.error ?: "Something went wrong",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Retry")
                }
            }
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

    val grouped = remember(state.upcomingTasks) {
        state.upcomingTasks.groupBy { it.dueDate }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = 8.dp),
    ) {
        grouped.forEach { (date, tasks) ->
            @Suppress("DEPRECATION")
            val sectionTitle = date?.let {
                "${it.month.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }} ${it.dayOfMonth}"
            } ?: "No date"
            item(key = "section-${date ?: "nodate"}") {
                Text(
                    text = sectionTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            items(tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onToggleComplete = viewModel::toggleComplete,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }
    }
}
