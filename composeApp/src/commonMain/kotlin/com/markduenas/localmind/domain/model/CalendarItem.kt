package com.markduenas.localmind.domain.model

sealed class CalendarItem {
    data class TaskItem(val task: Task) : CalendarItem()
    data class NoteItem(val note: Note) : CalendarItem()
}
