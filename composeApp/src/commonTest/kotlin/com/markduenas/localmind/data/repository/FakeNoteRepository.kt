package com.markduenas.localmind.data.repository

import com.markduenas.localmind.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate

class FakeNoteRepository : NoteRepository {

    val createdNotes = mutableListOf<Note>()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())

    override fun getAllNotes(): Flow<List<Note>> = _notes
    override fun getNoteById(id: String): Flow<Note?> = MutableStateFlow(
        _notes.value.find { it.id == id }
    )

    override suspend fun createNote(note: Note) {
        createdNotes.add(note)
        _notes.value = _notes.value + note
    }

    override suspend fun updateNote(note: Note) {
        _notes.value = _notes.value.map { if (it.id == note.id) note else it }
    }

    override suspend fun deleteNote(id: String) {
        _notes.value = _notes.value.filter { it.id != id }
    }

    override fun getNotesByDateRange(start: LocalDate, end: LocalDate): Flow<List<Note>> = _notes

    override fun searchNotes(query: String): Flow<List<Note>> = _notes
}
