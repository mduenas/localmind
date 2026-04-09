package com.markduenas.localmind.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.markduenas.localmind.domain.model.CalendarItem
import com.markduenas.localmind.domain.model.TaskStatus
import kotlinx.datetime.LocalDate

@Composable
fun DayEventList(
    date: LocalDate,
    items: List<CalendarItem>,
    onTaskClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
    Column(modifier = modifier) {
        Text(
            text = "$monthName ${date.day}, ${date.year}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        if (items.isEmpty()) {
            Text(
                text = "Nothing scheduled",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(items, key = { item ->
                    when (item) {
                        is CalendarItem.TaskItem -> "task-${item.task.id}"
                        is CalendarItem.NoteItem -> "note-${item.note.id}"
                    }
                }) { item ->
                    CalendarEventItem(
                        item = item,
                        onClick = {
                            when (item) {
                                is CalendarItem.TaskItem -> onTaskClick(item.task.id)
                                is CalendarItem.NoteItem -> onNoteClick(item.note.id)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
fun CalendarEventItem(
    item: CalendarItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = eventColor(item)
    val (icon, title, subtitle) = when (item) {
        is CalendarItem.TaskItem -> Triple(
            Icons.Default.CheckBox,
            item.task.title,
            item.task.dueTime?.let { t ->
                val hour = if (t.hour == 0) 12 else if (t.hour > 12) t.hour - 12 else t.hour
                val amPm = if (t.hour < 12) "AM" else "PM"
                val min = if (t.minute > 0) ":${t.minute.toString().padStart(2, '0')}" else ""
                "$hour$min $amPm"
            },
        )
        is CalendarItem.NoteItem -> Triple(
            Icons.AutoMirrored.Filled.StickyNote2,
            item.note.title,
            null,
        )
    }
    val isCompleted = item is CalendarItem.TaskItem && item.task.status == TaskStatus.COMPLETED

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(if (isCompleted) MaterialTheme.colorScheme.outline else color),
            )
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isCompleted) MaterialTheme.colorScheme.outline else color,
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCompleted) MaterialTheme.colorScheme.outline
                            else MaterialTheme.colorScheme.onSurface,
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
        }
    }
}
