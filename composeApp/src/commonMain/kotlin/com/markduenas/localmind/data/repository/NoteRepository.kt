package com.markduenas.localmind.data.repository

import com.markduenas.localmind.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getNoteById(id: String): Flow<Note?>
    suspend fun createNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: String)
    fun searchNotes(query: String): Flow<List<Note>>
}
