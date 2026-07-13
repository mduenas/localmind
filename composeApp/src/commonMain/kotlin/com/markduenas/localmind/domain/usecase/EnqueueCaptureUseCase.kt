package com.markduenas.localmind.domain.usecase

import com.benasher44.uuid.uuid4
import com.markduenas.localmind.data.repository.CaptureRepository
import com.markduenas.localmind.domain.model.Capture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.time.Clock

/**
 * Persists a raw capture immediately and kicks off background classification.
 * Uses its own [CoroutineScope] (rather than a caller-supplied one, e.g. a
 * ViewModel's) so processing survives the capture screen being popped right
 * after this returns.
 */
class EnqueueCaptureUseCase(
    private val captureRepository: CaptureRepository,
    private val processCaptureUseCase: ProcessCaptureUseCase,
) {
    private val processingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend operator fun invoke(rawText: String, audioPath: String? = null): String? {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return null

        val capture = Capture(
            id = uuid4().toString(),
            rawText = trimmed,
            audioPath = audioPath,
            createdAt = Clock.System.now(),
            processed = false,
        )
        captureRepository.save(capture)
        processingScope.launch { processCaptureUseCase(capture) }
        return capture.id
    }
}
