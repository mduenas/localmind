package com.markduenas.localmind.domain.usecase

import com.benasher44.uuid.uuid4
import com.markduenas.localmind.data.repository.NoteRepository
import com.markduenas.localmind.domain.model.Note
import com.markduenas.localmind.domain.model.ParsedNote
import kotlin.time.Clock

class CreateNoteUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(parsedNote: ParsedNote): Note {
        val now = Clock.System.now()
        val note = Note(
            id = uuid4().toString(),
            title = parsedNote.title,
            body = parsedNote.body,
            originalText = parsedNote.originalText,
            tags = emptyList(),
            createdAt = now,
            updatedAt = now,
            parsingConfidence = parsedNote.confidence
        )
        noteRepository.createNote(note)
        return note
    }
}
