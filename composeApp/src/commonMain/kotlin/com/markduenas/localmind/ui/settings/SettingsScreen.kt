package com.markduenas.localmind.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markduenas.localmind.ai.AIConfig
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
            downloadingSlug = (state.downloadState as? ModelDownloadState.Downloading)?.slug,
            onDownloadModel = viewModel::requestModelDownload,
            onDeleteModel = viewModel::deleteModel,
        )

        HorizontalDivider()

        NotificationSection(
            notificationsEnabled = state.notificationsEnabled,
            onNotificationsChanged = viewModel::setNotificationsEnabled,
        )

        HorizontalDivider()

        ExportSection()
    }

    // Model download dialogs
    when (val downloadState = state.downloadState) {
        is ModelDownloadState.ConfirmationRequired -> {
            val modelName = downloadState.slug
            val modelSize = AIConfig.MODEL_SIZES[modelName] ?: "unknown size"
            AlertDialog(
                onDismissRequest = viewModel::cancelDownload,
                title = { Text("Download AI Model") },
                text = {
                    Text(
                        "The $modelName model ($modelSize) needs to be downloaded. " +
                            "Download requires a Wi-Fi or cellular connection."
                    )
                },
                confirmButton = {
                    TextButton(onClick = viewModel::confirmDownload) {
                        Text("Download")
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::cancelDownload) {
                        Text("Cancel")
                    }
                },
            )
        }

        is ModelDownloadState.Downloading -> {
            AlertDialog(
                onDismissRequest = { /* non-dismissable */ },
                title = { Text("Downloading Model") },
                text = {
                    Column {
                        Text("Downloading ${downloadState.slug}…")
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = { /* no actions while downloading */ },
            )
        }

        is ModelDownloadState.Failed -> {
            AlertDialog(
                onDismissRequest = viewModel::dismissDownloadError,
                title = { Text("Download Failed") },
                text = { Text(downloadState.error) },
                confirmButton = {
                    TextButton(onClick = viewModel::confirmDownload) {
                        Text("Retry")
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissDownloadError) {
                        Text("Cancel")
                    }
                },
            )
        }

        is ModelDownloadState.Idle -> { /* no dialog */ }
    }
}
