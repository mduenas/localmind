package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.data.repository.NoteRepository
import com.markduenas.localmind.data.repository.TaskRepository
import com.markduenas.localmind.domain.model.CalendarItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class GetCalendarItemsUseCase(
    private val taskRepository: TaskRepository,
    private val noteRepository: NoteRepository,
) {
    operator fun invoke(start: LocalDate, end: LocalDate): Flow<Map<LocalDate, List<CalendarItem>>> {
        return combine(
            taskRepository.getTasksByDateRange(start, end),
            noteRepository.getNotesByDateRange(start, end),
        ) { tasks, notes ->
            val map = mutableMapOf<LocalDate, MutableList<CalendarItem>>()
            tasks.forEach { task ->
                task.dueDate?.let { date ->
                    map.getOrPut(date) { mutableListOf() }.add(CalendarItem.TaskItem(task))
                }
            }
            val tz = TimeZone.currentSystemDefault()
            notes.forEach { note ->
                val noteDate = note.createdAt.toLocalDateTime(tz).date
                map.getOrPut(noteDate) { mutableListOf() }.add(CalendarItem.NoteItem(note))
            }
            map.mapValues { (_, v) -> v.toList() }
        }
    }
}
