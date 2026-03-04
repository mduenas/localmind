package com.markduenas.localmind.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markduenas.localmind.ai.AIConfig
import com.markduenas.localmind.ai.ModelDownloadState
import kotlin.math.roundToInt

@Composable
fun ModelManagementSection(
    downloadedModels: List<String>,
    availableModels: List<String>,
    selectedLlmModel: String,
    downloadState: ModelDownloadState,
    onDownloadModel: (String) -> Unit,
    onDeleteModel: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onRetryDownload: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val llmModels = availableModels.filter { it !in AIConfig.STT_MODELS }
    val sttModels = availableModels.filter { it in AIConfig.STT_MODELS }

    Column(modifier = modifier.fillMaxWidth()) {
        // LLM Models section
        Text(
            text = "LLM Models",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Text(
            text = "Select which model to use for task parsing",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        llmModels.forEach { model ->
            ModelRow(
                model = model,
                isDownloaded = model in downloadedModels,
                isSelected = model in downloadedModels && model == selectedLlmModel,
                isSTT = false,
                downloadState = downloadState,
                onDownloadModel = onDownloadModel,
                onDeleteModel = onDeleteModel,
                onSelectModel = onSelectModel,
                onRetryDownload = onRetryDownload,
                onDismissError = onDismissError,
            )
        }

        // STT Models section
        if (sttModels.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Speech-to-Text",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            sttModels.forEach { model ->
                ModelRow(
                    model = model,
                    isDownloaded = model in downloadedModels,
                    isSelected = false,
                    isSTT = true,
                    downloadState = downloadState,
                    onDownloadModel = onDownloadModel,
                    onDeleteModel = onDeleteModel,
                    onSelectModel = onSelectModel,
                    onRetryDownload = onRetryDownload,
                    onDismissError = onDismissError,
                )
            }
        }
    }
}

@Composable
private fun ModelRow(
    model: String,
    isDownloaded: Boolean,
    isSelected: Boolean,
    isSTT: Boolean,
    downloadState: ModelDownloadState,
    onDownloadModel: (String) -> Unit,
    onDeleteModel: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onRetryDownload: () -> Unit,
    onDismissError: () -> Unit,
) {
    val isDownloading = downloadState is ModelDownloadState.Downloading &&
        downloadState.slug == model
    val isFailed = downloadState is ModelDownloadState.Failed &&
        downloadState.slug == model
    val isAnyDownloading = downloadState is ModelDownloadState.Downloading
    val sizeLabel = AIConfig.MODEL_SIZES[model]

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = model,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Active model",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Text(
                    text = when {
                        isDownloading && downloadState is ModelDownloadState.Downloading -> {
                            if (downloadState.progress >= 0f) {
                                "Downloading… ${(downloadState.progress * 100).roundToInt()}%"
                            } else {
                                "Downloading…"
                            }
                        }
                        isFailed && downloadState is ModelDownloadState.Failed -> downloadState.error
                        isSelected -> "Active"
                        isDownloaded -> "Downloaded"
                        sizeLabel != null -> "Not downloaded ($sizeLabel)"
                        else -> "Not downloaded"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        isFailed -> MaterialTheme.colorScheme.error
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            when {
                isDownloaded -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!isSTT && !isSelected) {
                            Button(onClick = { onSelectModel(model) }) {
                                Text("Use")
                            }
                        }
                        OutlinedButton(onClick = { onDeleteModel(model) }) {
                            Text("Delete")
                        }
                    }
                }
                isFailed -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismissError) {
                            Text("Dismiss")
                        }
                        Button(onClick = onRetryDownload) {
                            Text("Retry")
                        }
                    }
                }
                !isDownloading && !isAnyDownloading -> {
                    Button(onClick = { onDownloadModel(model) }) {
                        Text("Download")
                    }
                }
            }
        }

        if (isDownloading && downloadState is ModelDownloadState.Downloading) {
            Spacer(Modifier.height(4.dp))
            if (downloadState.progress >= 0f) {
                LinearProgressIndicator(
                    progress = { downloadState.progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
