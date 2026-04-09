package com.markduenas.localmind.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markduenas.localmind.domain.usecase.GetCalendarItemsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class CalendarViewModel(
    private val getCalendarItemsUseCase: GetCalendarItemsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CalendarUiState(
            currentMonthStart = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault()).let {
                @Suppress("DEPRECATION")
                LocalDate(it.year, it.monthNumber, 1)
            }
        )
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var itemsJob: Job? = null

    init {
        loadItemsForCurrentMonth()
    }

    private fun loadItemsForCurrentMonth() {
        itemsJob?.cancel()
        itemsJob = viewModelScope.launch {
            val monthStart = _uiState.value.currentMonthStart
            val monthEnd = monthStart.monthEnd()
            getCalendarItemsUseCase(monthStart, monthEnd)
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { items ->
                    _uiState.update { it.copy(itemsByDate = items, isLoading = false) }
                }
        }
    }

    fun nextMonth() {
        _uiState.update {
            it.copy(
                currentMonthStart = it.currentMonthStart.nextMonthStart(),
                selectedDate = null,
                isLoading = true,
            )
        }
        loadItemsForCurrentMonth()
    }

    fun prevMonth() {
        _uiState.update {
            it.copy(
                currentMonthStart = it.currentMonthStart.prevMonthStart(),
                selectedDate = null,
                isLoading = true,
            )
        }
        loadItemsForCurrentMonth()
    }

    fun selectDate(date: LocalDate) {
        _uiState.update {
            it.copy(selectedDate = if (it.selectedDate == date) null else date)
        }
    }

    fun toggleViewMode() {
        _uiState.update {
            it.copy(
                viewMode = when (it.viewMode) {
                    CalendarViewMode.MONTH -> CalendarViewMode.WEEK
                    CalendarViewMode.WEEK -> CalendarViewMode.MONTH
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

@Suppress("DEPRECATION")
internal fun LocalDate.nextMonthStart(): LocalDate =
    if (monthNumber == 12) LocalDate(year + 1, 1, 1)
    else LocalDate(year, monthNumber + 1, 1)

@Suppress("DEPRECATION")
internal fun LocalDate.prevMonthStart(): LocalDate =
    if (monthNumber == 1) LocalDate(year - 1, 12, 1)
    else LocalDate(year, monthNumber - 1, 1)

internal fun LocalDate.monthEnd(): LocalDate =
    LocalDate.fromEpochDays(nextMonthStart().toEpochDays() - 1)

internal fun LocalDate.weekStart(): LocalDate =
    LocalDate.fromEpochDays(toEpochDays() - ((dayOfWeek.ordinal + 1) % 7))
