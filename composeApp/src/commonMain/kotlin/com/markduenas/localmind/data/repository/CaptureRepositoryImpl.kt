package com.markduenas.localmind.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.markduenas.localmind.data.local.LocalMindDb
import com.markduenas.localmind.data.local.toDomainCapture
import com.markduenas.localmind.domain.model.Capture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CaptureRepositoryImpl(
    private val db: LocalMindDb
) : CaptureRepository {

    private val queries get() = db.localMindDbQueries

    override suspend fun save(capture: Capture) {
        queries.insertCapture(
            id = capture.id,
            raw_text = capture.rawText,
            audio_path = capture.audioPath,
            created_at = capture.createdAt.toEpochMilliseconds(),
            processed = if (capture.processed) 1L else 0L
        )
    }

    override fun getUnprocessed(): Flow<List<Capture>> {
        return queries.getUnprocessedCaptures()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { captures -> captures.map { it.toDomainCapture() } }
    }

    override suspend fun markProcessed(id: String) {
        queries.markCaptureProcessed(id)
    }
}
