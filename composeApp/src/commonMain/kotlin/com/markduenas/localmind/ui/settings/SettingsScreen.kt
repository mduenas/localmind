package com.markduenas.localmind.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // LLM toggle
        Column {
            Text(
                text = "AI Parsing",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "On-Device LLM",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "Use AI model for smarter task parsing (requires model download)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.llmEnabled,
                    onCheckedChange = viewModel::setLlmEnabled,
                )
            }
        }

        HorizontalDivider()

        ModelManagementSection(
            downloadedModels = state.downloadedModels,
            availableModels = state.availableModels,
            downloadState = state.downloadState,
            onDownloadModel = viewModel::requestModelDownload,
            onDeleteModel = viewModel::deleteModel,
            onRetryDownload = viewModel::retryDownload,
            onDismissError = viewModel::dismissDownloadError,
        )

        HorizontalDivider()

        NotificationSection(
            notificationsEnabled = state.notificationsEnabled,
            onNotificationsChanged = viewModel::setNotificationsEnabled,
        )

        HorizontalDivider()

        ExportSection(onExportTasks = viewModel::exportTasks)
    }
}
