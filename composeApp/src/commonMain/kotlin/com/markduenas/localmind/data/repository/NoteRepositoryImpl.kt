package com.markduenas.localmind.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.markduenas.localmind.data.local.LocalMindDb
import com.markduenas.localmind.data.local.toDomainNote
import com.markduenas.localmind.domain.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class NoteRepositoryImpl(
    private val db: LocalMindDb
) : NoteRepository {

    private val queries get() = db.localMindDbQueries

    override fun getAllNotes(): Flow<List<Note>> {
        return queries.getAllNotes()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { notes -> notes.map { it.toDomainNote() } }
    }

    override fun getNoteById(id: String): Flow<Note?> {
        return queries.getNoteById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomainNote() }
    }

    override suspend fun createNote(note: Note) {
        queries.insertNote(
            id = note.id,
            title = note.title,
            body = note.body,
            original_text = note.originalText,
            created_at = note.createdAt.toEpochMilliseconds(),
            updated_at = note.updatedAt.toEpochMilliseconds(),
            parsing_confidence = note.parsingConfidence?.toDouble()
        )
    }

    override suspend fun updateNote(note: Note) {
        queries.updateNote(
            title = note.title,
            body = note.body,
            updated_at = Clock.System.now().toEpochMilliseconds(),
            id = note.id
        )
    }

    override suspend fun deleteNote(id: String) {
        queries.deleteNote(id)
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        val searchQuery = "%$query%"
        return queries.searchNotes(searchQuery, searchQuery, searchQuery)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { notes -> notes.map { it.toDomainNote() } }
    }
}
