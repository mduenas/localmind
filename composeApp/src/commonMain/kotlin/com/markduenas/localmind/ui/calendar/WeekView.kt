package com.markduenas.localmind.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.markduenas.localmind.domain.model.CalendarItem
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

private val WEEK_DAY_LABELS = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

@Composable
fun WeekView(
    anchorDate: LocalDate,
    selectedDate: LocalDate?,
    itemsByDate: Map<LocalDate, List<CalendarItem>>,
    onDayClick: (LocalDate) -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())
    val weekStart = anchorDate.weekStart()
    val weekDates = (0..6).map { LocalDate.fromEpochDays(weekStart.toEpochDays() + it) }

    val monthLabel = weekStart.month.name.lowercase().replaceFirstChar { it.uppercase() }
    @Suppress("DEPRECATION")
    val weekLabel = if (weekStart.monthNumber == weekDates.last().monthNumber) {
        "$monthLabel ${weekStart.year}"
    } else {
        val endMonth = weekDates.last().month.name.lowercase().replaceFirstChar { it.uppercase() }
        "$monthLabel – $endMonth ${weekDates.last().year}"
    }

    Column(modifier = modifier) {
        // Week navigation header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onPrevWeek) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous week")
            }
            Text(
                text = weekLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = onNextWeek) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next week")
            }
        }

        // 7 columns
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            weekDates.forEachIndexed { index, date ->
                val isToday = date == today
                val isSelected = date == selectedDate
                val dayItems = itemsByDate[date] ?: emptyList()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(MaterialTheme.shapes.small)
                        .then(
                            if (isSelected) Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                            else Modifier
                        )
                        .then(
                            if (isToday && !isSelected) Modifier.border(
                                1.dp,
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.shapes.small
                            ) else Modifier
                        )
                        .clickable { onDayClick(date) }
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = WEEK_DAY_LABELS[index],
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = date.day.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    // Event chips — show up to 3
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        dayItems.take(3).forEach { item ->
                            val chipColor = eventColor(item)
                            val label = when (item) {
                                is CalendarItem.TaskItem -> item.task.title
                                is CalendarItem.NoteItem -> item.note.title
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(chipColor.copy(alpha = 0.15f), MaterialTheme.shapes.extraSmall)
                                    .padding(horizontal = 2.dp, vertical = 1.dp),
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = chipColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                        if (dayItems.size > 3) {
                            Text(
                                text = "+${dayItems.size - 3}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}
