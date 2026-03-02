package com.markduenas.localmind.domain.model

sealed class ParsedCapture {
    data class TaskCapture(val task: ParsedTask) : ParsedCapture()
    data class NoteCapture(val note: ParsedNote) : ParsedCapture()
}
