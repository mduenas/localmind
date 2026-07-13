package com.markduenas.localmind.domain.usecase

import com.markduenas.localmind.ai.RuleBasedParser
import com.markduenas.localmind.data.repository.CaptureRepository
import com.markduenas.localmind.domain.model.Capture
import com.markduenas.localmind.domain.model.ParsedCapture

/**
 * Classifies a single raw capture into a Note or Task and persists it.
 * Rule-based only for now (Phase 1) — never throws for expected parse failures,
 * since [RuleBasedParser] is pure regex and always returns a result. On unexpected
 * failure (e.g. a DB error), the capture is left unprocessed so it can be retried
 * on the next drain instead of being silently lost.
 */
class ProcessCaptureUseCase(
    private val ruleBasedParser: RuleBasedParser,
    private val createNoteUseCase: CreateNoteUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val captureRepository: CaptureRepository,
) {
    suspend operator fun invoke(capture: Capture) {
        if (capture.processed) return

        runCatching {
            when (val parsed = ruleBasedParser.parse(capture.rawText)) {
                is ParsedCapture.NoteCapture -> createNoteUseCase(parsed.note)
                is ParsedCapture.TaskCapture -> createTaskUseCase(parsed.task)
            }
            captureRepository.markProcessed(capture.id)
        }
    }
}
