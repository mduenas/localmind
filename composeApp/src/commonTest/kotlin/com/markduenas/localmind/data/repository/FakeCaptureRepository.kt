package com.markduenas.localmind.data.repository

import com.markduenas.localmind.domain.model.Capture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCaptureRepository : CaptureRepository {

    val savedCaptures = mutableListOf<Capture>()
    val markedProcessedIds = mutableListOf<String>()

    private val _captures = MutableStateFlow<List<Capture>>(emptyList())

    override suspend fun save(capture: Capture) {
        savedCaptures.add(capture)
        _captures.value = _captures.value + capture
    }

    override fun getUnprocessed(): Flow<List<Capture>> = _captures

    override suspend fun markProcessed(id: String) {
        markedProcessedIds.add(id)
        _captures.value = _captures.value.filter { it.id != id }
    }
}
