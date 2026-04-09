package com.markduenas.localmind.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.markduenas.localmind.domain.model.CalendarItem
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

private val DAY_LABELS = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

@Composable
fun MonthGrid(
    monthStart: LocalDate,
    selectedDate: LocalDate?,
    itemsByDate: Map<LocalDate, List<CalendarItem>>,
    onDayClick: (LocalDate) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())
    val monthName = monthStart.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val year = monthStart.year
    val daysInMonth = monthStart.monthEnd().day
    // ISO ordinal: Mon=0..Sun=6; Sunday-first offset: Sun→0, Mon→1, ..., Sat→6
    val firstDayOffset = (monthStart.dayOfWeek.ordinal + 1) % 7
    val totalCells = 42 // 6 rows × 7 cols

    Column(modifier = modifier) {
        // Month/Year header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
            }
            Text(
                text = "$monthName $year",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
            }
        }

        // Day-of-week header
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            DAY_LABELS.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Day grid — 6 rows of 7
        repeat(6) { row ->
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val dayOfMonth = cellIndex - firstDayOffset + 1
                    val isCurrentMonth = dayOfMonth in 1..daysInMonth

                    val date = when {
                        dayOfMonth < 1 -> {
                            val prevMonthEnd = monthStart.prevMonthStart().monthEnd()
                            LocalDate.fromEpochDays(prevMonthEnd.toEpochDays() + dayOfMonth)
                        }
                        dayOfMonth > daysInMonth -> {
                            val nextStart = monthStart.nextMonthStart()
                            LocalDate.fromEpochDays(nextStart.toEpochDays() + (dayOfMonth - daysInMonth - 1))
                        }
                        else -> {
                            @Suppress("DEPRECATION")
                            LocalDate(monthStart.year, monthStart.monthNumber, dayOfMonth)
                        }
                    }

                    CalendarDayCell(
                        date = date,
                        isCurrentMonth = isCurrentMonth,
                        isToday = date == today,
                        isSelected = date == selectedDate,
                        items = itemsByDate[date] ?: emptyList(),
                        onClick = { onDayClick(date) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
