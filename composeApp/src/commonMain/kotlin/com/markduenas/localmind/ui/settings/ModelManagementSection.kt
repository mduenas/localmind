package com.markduenas.localmind.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markduenas.localmind.ai.AIConfig

@Composable
fun ModelManagementSection(
    downloadedModels: List<String>,
    availableModels: List<String>,
    downloadingSlug: String?,
    onDownloadModel: (String) -> Unit,
    onDeleteModel: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "AI Models",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        if (downloadedModels.isEmpty()) {
            Text(
                text = "No models downloaded",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        availableModels.forEach { model ->
            val isDownloaded = model in downloadedModels
            val isDownloading = model == downloadingSlug
            val sizeLabel = AIConfig.MODEL_SIZES[model]
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
                            isDownloading -> "Downloading…"
                            isDownloaded -> "Downloaded"
                            sizeLabel != null -> "Not downloaded ($sizeLabel)"
                            else -> "Not downloaded"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (isDownloaded) {
                    OutlinedButton(onClick = { onDeleteModel(model) }) {
                        Text("Delete")
                    }
                } else if (!isDownloading) {
                    Button(onClick = { onDownloadModel(model) }) {
                        Text("Download")
                    }
                }
            }
        }
    }
}
