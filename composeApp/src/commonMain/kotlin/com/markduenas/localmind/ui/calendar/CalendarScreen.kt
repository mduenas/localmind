package com.markduenas.localmind.ui.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CalendarScreen(
    onNavigateToTask: (String) -> Unit,
    onNavigateToNote: (String) -> Unit,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // View mode toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                FilterChip(
                    selected = state.viewMode == CalendarViewMode.MONTH,
                    onClick = {
                        if (state.viewMode != CalendarViewMode.MONTH) viewModel.toggleViewMode()
                    },
                    label = { Text("Month") },
                    modifier = Modifier.padding(end = 8.dp),
                )
                FilterChip(
                    selected = state.viewMode == CalendarViewMode.WEEK,
                    onClick = {
                        if (state.viewMode != CalendarViewMode.WEEK) viewModel.toggleViewMode()
                    },
                    label = { Text("Week") },
                )
            }

            // Month or Week view
            when (state.viewMode) {
                CalendarViewMode.MONTH -> MonthGrid(
                    monthStart = state.currentMonthStart,
                    selectedDate = state.selectedDate,
                    itemsByDate = state.itemsByDate,
                    onDayClick = viewModel::selectDate,
                    onPrevMonth = viewModel::prevMonth,
                    onNextMonth = viewModel::nextMonth,
                    modifier = Modifier.fillMaxWidth(),
                )
                CalendarViewMode.WEEK -> {
                    val anchor = state.selectedDate ?: state.currentMonthStart
                    WeekView(
                        anchorDate = anchor,
                        selectedDate = state.selectedDate,
                        itemsByDate = state.itemsByDate,
                        onDayClick = viewModel::selectDate,
                        onPrevWeek = {
                            val prevWeekDate = LocalDate.fromEpochDays(anchor.toEpochDays() - 7)
                            @Suppress("DEPRECATION")
                            if (prevWeekDate.year < state.currentMonthStart.year ||
                                prevWeekDate.monthNumber < state.currentMonthStart.monthNumber) {
                                viewModel.prevMonth()
                            }
                            viewModel.selectDate(prevWeekDate)
                        },
                        onNextWeek = {
                            val nextWeekDate = LocalDate.fromEpochDays(anchor.toEpochDays() + 7)
                            @Suppress("DEPRECATION")
                            if (nextWeekDate.year > state.currentMonthStart.year ||
                                nextWeekDate.monthNumber > state.currentMonthStart.monthNumber) {
                                viewModel.nextMonth()
                            }
                            viewModel.selectDate(nextWeekDate)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Day event list — shown when a date is selected
            state.selectedDate?.let { date ->
                val items = state.itemsByDate[date] ?: emptyList()
                DayEventList(
                    date = date,
                    items = items,
                    onTaskClick = onNavigateToTask,
                    onNoteClick = onNavigateToNote,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
}
