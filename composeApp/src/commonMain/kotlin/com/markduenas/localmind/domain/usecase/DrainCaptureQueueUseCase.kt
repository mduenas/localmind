package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.data.repository.CaptureRepository
import kotlinx.coroutines.flow.first

/**
 * Processes any captures left unprocessed from a previous session (app killed
 * mid-process, or a share-sheet capture inserted while the app was closed).
 * Call once on app start so pending captures never strand.
 */
class DrainCaptureQueueUseCase(
    private val captureRepository: CaptureRepository,
    private val processCaptureUseCase: ProcessCaptureUseCase,
) {
    suspend operator fun invoke() {
        val pending = captureRepository.getUnprocessed().first()
        for (capture in pending) {
            processCaptureUseCase(capture)
        }
    }
}
