package com.markduenas.localmind.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markduenas.localmind.domain.model.Priority
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FieldEditor(
    title: String,
    onTitleChanged: (String) -> Unit,
    dueDate: LocalDate?,
    onDueDateChanged: (LocalDate?) -> Unit,
    dueTime: LocalTime?,
    onDueTimeChanged: (LocalTime?) -> Unit,
    priority: Priority,
    onPriorityChanged: (Priority) -> Unit,
    tags: List<String>,
    onTagsChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChanged,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = dueDate?.toString() ?: "",
            onValueChange = { text ->
                onDueDateChanged(
                    try { LocalDate.parse(text) } catch (_: Exception) { null }
                )
            },
            label = { Text("Due Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g. 2026-02-17") },
        )

        OutlinedTextField(
            value = dueTime?.let { "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}" } ?: "",
            onValueChange = { text ->
                onDueTimeChanged(
                    try {
                        val parts = text.split(":")
                        if (parts.size == 2) LocalTime(parts[0].toInt(), parts[1].toInt()) else null
                    } catch (_: Exception) { null }
                )
            },
            label = { Text("Due Time (HH:MM)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g. 15:00") },
        )

        Column {
            Text(
                text = "Priority",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { onPriorityChanged(p) },
                        label = { Text(p.name) },
                    )
                }
            }
        }

        if (tags.isNotEmpty()) {
            Column {
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag ->
                        FilterChip(
                            selected = true,
                            onClick = { onTagsChanged(tags - tag) },
                            label = { Text(tag) },
                        )
                    }
                }
            }
        }
    }
}
