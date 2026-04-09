package com.markduenas.localmind.ui.calendar

import com.markduenas.localmind.domain.model.CalendarItem
import kotlinx.datetime.LocalDate

enum class CalendarViewMode { MONTH, WEEK }

data class CalendarUiState(
    val currentMonthStart: LocalDate,
    val selectedDate: LocalDate? = null,
    val viewMode: CalendarViewMode = CalendarViewMode.MONTH,
    val itemsByDate: Map<LocalDate, List<CalendarItem>> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
)
