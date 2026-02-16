package com.markduenas.localmind.data.repository

import com.markduenas.localmind.domain.model.Capture
import kotlinx.coroutines.flow.Flow

interface CaptureRepository {
    suspend fun save(capture: Capture)
    fun getUnprocessed(): Flow<List<Capture>>
    suspend fun markProcessed(id: String)
}
