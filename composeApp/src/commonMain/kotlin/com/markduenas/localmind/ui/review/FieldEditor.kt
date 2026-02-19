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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

        // Date field — keep raw text in local state so partial input isn't cleared
        var dateText by remember { mutableStateOf(dueDate?.toString() ?: "") }
        var dateIsValid by remember { mutableStateOf(true) }

        // Sync from external state (e.g. initial value from parser)
        LaunchedEffect(dueDate) {
            val formatted = dueDate?.toString() ?: ""
            if (formatted != dateText) {
                // Only overwrite if the external value actually changed
                val currentParsed = try { LocalDate.parse(dateText) } catch (_: Exception) { null }
                if (currentParsed != dueDate) {
                    dateText = formatted
                    dateIsValid = true
                }
            }
        }

        OutlinedTextField(
            value = dateText,
            onValueChange = { text ->
                dateText = text
                if (text.isBlank()) {
                    dateIsValid = true
                    onDueDateChanged(null)
                } else {
                    val parsed = try { LocalDate.parse(text) } catch (_: Exception) { null }
                    dateIsValid = parsed != null
                    if (parsed != null) {
                        onDueDateChanged(parsed)
                    }
                }
            },
            label = { Text("Due Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g. 2026-02-17") },
            isError = !dateIsValid && dateText.isNotBlank(),
            supportingText = if (!dateIsValid && dateText.isNotBlank()) {
                { Text("Use format YYYY-MM-DD") }
            } else null,
        )

        // Time field — same approach with local text state
        var timeText by remember {
            mutableStateOf(
                dueTime?.let {
                    "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
                } ?: ""
            )
        }
        var timeIsValid by remember { mutableStateOf(true) }

        LaunchedEffect(dueTime) {
            val formatted = dueTime?.let {
                "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
            } ?: ""
            if (formatted != timeText) {
                val currentParsed = parseTime(timeText)
                if (currentParsed != dueTime) {
                    timeText = formatted
                    timeIsValid = true
                }
            }
        }

        OutlinedTextField(
            value = timeText,
            onValueChange = { text ->
                timeText = text
                if (text.isBlank()) {
                    timeIsValid = true
                    onDueTimeChanged(null)
                } else {
                    val parsed = parseTime(text)
                    timeIsValid = parsed != null
                    if (parsed != null) {
                        onDueTimeChanged(parsed)
                    }
                }
            },
            label = { Text("Due Time (HH:MM)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g. 15:00") },
            isError = !timeIsValid && timeText.isNotBlank(),
            supportingText = if (!timeIsValid && timeText.isNotBlank()) {
                { Text("Use format HH:MM") }
            } else null,
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

private fun parseTime(text: String): LocalTime? {
    return try {
        val parts = text.split(":")
        if (parts.size == 2) LocalTime(parts[0].toInt(), parts[1].toInt()) else null
    } catch (_: Exception) {
        null
    }
}
