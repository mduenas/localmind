package com.markduenas.localmind.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import kotlin.math.roundToInt

@Composable
fun ModelManagementSection(
    downloadedModels: List<String>,
    availableModels: List<String>,
    downloadState: ModelDownloadState,
    onDownloadModel: (String) -> Unit,
    onDeleteModel: (String) -> Unit,
    onRetryDownload: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "AI Models",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        if (downloadedModels.isEmpty() && downloadState is ModelDownloadState.Idle) {
            Text(
                text = "No models downloaded",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        availableModels.forEach { model ->
            val isDownloaded = model in downloadedModels
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
                        Text(
                            text = model,
                            style = MaterialTheme.typography.bodyMedium,
                        )
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
                                isDownloaded -> "Downloaded"
                                sizeLabel != null -> "Not downloaded ($sizeLabel)"
                                else -> "Not downloaded"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                isFailed -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    when {
                        isDownloaded -> {
                            OutlinedButton(onClick = { onDeleteModel(model) }) {
                                Text("Delete")
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
    }
}
