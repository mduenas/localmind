package com.markduenas.localmind.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markduenas.localmind.domain.model.ParseResult
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ParseReviewScreen(
    captureText: String,
    onSaved: (SaveResult) -> Unit,
    onDiscard: () -> Unit,
    viewModel: ParseReviewViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(captureText) {
        viewModel.parseCapture(captureText)
    }

    val isNote = state.captureType == CaptureType.NOTE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNote) "Review Note" else "Review Task") },
                navigationIcon = {
                    IconButton(onClick = onDiscard) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Parsing your input...")
                    }
                }
            }
            state.error != null -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Failed to parse",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(state.error ?: "")
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = onDiscard) {
                                Text("Go Back")
                            }
                            Button(onClick = { viewModel.retryParse() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    // Original text
                    Text(
                        text = "Original",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "\"${state.originalText}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    // Parse confidence card (only for tasks)
                    if (!isNote) {
                        state.parsedTask?.let { parsed ->
                            TaskPreviewCard(parsedTask = parsed)
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    if (state.parseResult is ParseResult.Fallback) {
                        Text(
                            text = "Used fallback parser: ${(state.parseResult as ParseResult.Fallback).reason ?: "LLM unavailable"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    if (state.isEnhancing) {
                        Text(
                            text = "Refining with AI...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Task/Note type toggle
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        CaptureType.entries.forEachIndexed { index, type ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = CaptureType.entries.size,
                                ),
                                onClick = { viewModel.onTypeChanged(type) },
                                selected = state.captureType == type,
                            ) {
                                Text(if (type == CaptureType.TASK) "Task" else "Note")
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    val suggestedTitle = state.suggestedTitle
                    if (!suggestedTitle.isNullOrBlank()) {
                        Text(
                            text = "AI suggestion: $suggestedTitle",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(onClick = viewModel::applySuggestedTitle) {
                            Text("Use Suggested Title")
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Editable fields — conditional on type
                    if (isNote) {
                        // Note fields: title + body + tags
                        OutlinedTextField(
                            value = state.editedTitle,
                            onValueChange = viewModel::onTitleChanged,
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.editedBody,
                            onValueChange = viewModel::onBodyChanged,
                            label = { Text("Body") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 8,
                        )
                        if (state.editedTags.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Tags",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.editedTags.forEach { tag ->
                                    FilterChip(
                                        selected = true,
                                        onClick = { viewModel.onTagsChanged(state.editedTags - tag) },
                                        label = { Text(tag) },
                                    )
                                }
                            }
                        }
                    } else {
                        // Task fields
                        FieldEditor(
                            title = state.editedTitle,
                            onTitleChanged = viewModel::onTitleChanged,
                            dueDate = state.editedDueDate,
                            onDueDateChanged = viewModel::onDueDateChanged,
                            dueTime = state.editedDueTime,
                            onDueTimeChanged = viewModel::onDueTimeChanged,
                            priority = state.editedPriority,
                            onPriorityChanged = viewModel::onPriorityChanged,
                            tags = state.editedTags,
                            onTagsChanged = viewModel::onTagsChanged,
                        )
                    }

                    // Inference log toggle
                    val log = state.inferenceLog
                    if (log != null) {
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = viewModel::toggleInferenceLog) {
                            Text(
                                if (state.showInferenceLog) "Hide Log" else "Show Log",
                            )
                        }
                        if (state.showInferenceLog) {
                            InferenceLogSection(log = log)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = onDiscard,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Discard")
                        }
                        Button(
                            onClick = { viewModel.save(onSaved) },
                            modifier = Modifier.weight(1f),
                            enabled = state.editedTitle.isNotBlank() && !state.isSaving,
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                )
                            } else {
                                Text(if (isNote) "Save Note" else "Save Task")
                            }
                        }
                    }
                }
            }
        }
    }
}
