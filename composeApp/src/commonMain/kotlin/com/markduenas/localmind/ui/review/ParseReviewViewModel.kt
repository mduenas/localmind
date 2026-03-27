package com.markduenas.localmind.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markduenas.localmind.ai.InferenceLog
import com.markduenas.localmind.domain.model.ParseResult
import com.markduenas.localmind.domain.model.ParsedCapture
import com.markduenas.localmind.domain.model.ParsedNote
import com.markduenas.localmind.domain.model.ParsedTask
import com.markduenas.localmind.domain.model.Priority
import com.markduenas.localmind.domain.usecase.CreateNoteUseCase
import com.markduenas.localmind.domain.usecase.CreateTaskUseCase
import com.markduenas.localmind.domain.usecase.ParseCaptureUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

enum class CaptureType { TASK, NOTE }

sealed class SaveResult {
    data class TaskSaved(val dueDate: LocalDate?) : SaveResult()
    data object NoteSaved : SaveResult()
}

data class ParseReviewUiState(
    val isLoading: Boolean = true,
    val isEnhancing: Boolean = false,
    val originalText: String = "",
    val captureType: CaptureType = CaptureType.TASK,
    val parsedTask: ParsedTask? = null,
    val parseResult: ParseResult? = null,
    val inferenceLog: InferenceLog? = null,
    val showInferenceLog: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val editedTitle: String = "",
    val editedBody: String = "",
    val editedDueDate: LocalDate? = null,
    val editedDueTime: LocalTime? = null,
    val editedPriority: Priority = Priority.MEDIUM,
    val editedTags: List<String> = emptyList(),
    val titleTouched: Boolean = false,
    val bodyTouched: Boolean = false,
    val dueDateTouched: Boolean = false,
    val dueTimeTouched: Boolean = false,
    val priorityTouched: Boolean = false,
    val tagsTouched: Boolean = false,
    val typeTouched: Boolean = false,
    val suggestedTitle: String? = null,
)

class ParseReviewViewModel(
    private val parseCaptureUseCase: ParseCaptureUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParseReviewUiState())
    val uiState: StateFlow<ParseReviewUiState> = _uiState.asStateFlow()

    fun parseCapture(rawText: String) {
        _uiState.update {
            it.copy(
                isLoading = true,
                isEnhancing = false,
                originalText = rawText,
                error = null,
                suggestedTitle = null,
                titleTouched = false,
                bodyTouched = false,
                dueDateTouched = false,
                dueTimeTouched = false,
                priorityTouched = false,
                tagsTouched = false,
                typeTouched = false,
            )
        }
        viewModelScope.launch {
            val immediate = parseCaptureUseCase.parseImmediate(rawText)
            val immediateLog = when (immediate) {
                is ParseResult.Success -> immediate.inferenceLog
                is ParseResult.Fallback -> immediate.inferenceLog
                is ParseResult.Error -> null
            }
            when (immediate) {
                is ParseResult.Success -> applyInitialParsed(immediate.capture, immediate, immediateLog)
                is ParseResult.Fallback -> applyInitialParsed(immediate.capture, immediate, immediateLog)
                is ParseResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = immediate.message) }
                    return@launch
                }
            }

            _uiState.update { it.copy(isEnhancing = true) }
            val enhanced = parseCaptureUseCase.parseEnhancement(rawText)
            when (enhanced) {
                is ParseResult.Success -> mergeEnhanced(enhanced.capture, enhanced, enhanced.inferenceLog)
                is ParseResult.Fallback -> {
                    _uiState.update { state ->
                        state.copy(
                            parseResult = enhanced,
                            inferenceLog = enhanced.inferenceLog ?: state.inferenceLog,
                        )
                    }
                }
                is ParseResult.Error -> _uiState.update { it.copy(error = enhanced.message) }
                null -> Unit
            }
            _uiState.update { it.copy(isEnhancing = false) }
        }
    }

    private fun applyInitialParsed(capture: ParsedCapture, result: ParseResult, log: InferenceLog?) {
        when (capture) {
            is ParsedCapture.TaskCapture -> {
                val task = capture.task
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        captureType = CaptureType.TASK,
                        parsedTask = task,
                        parseResult = result,
                        inferenceLog = log,
                        editedTitle = task.title,
                        editedBody = "",
                        editedDueDate = task.dueDate,
                        editedDueTime = task.dueTime,
                        editedPriority = task.priority,
                        editedTags = task.tags,
                        titleTouched = false,
                        bodyTouched = false,
                        dueDateTouched = false,
                        dueTimeTouched = false,
                        priorityTouched = false,
                        tagsTouched = false,
                        typeTouched = false,
                        suggestedTitle = null,
                    )
                }
            }
            is ParsedCapture.NoteCapture -> {
                val note = capture.note
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        captureType = CaptureType.NOTE,
                        parsedTask = null,
                        parseResult = result,
                        inferenceLog = log,
                        editedTitle = note.title,
                        editedBody = note.body,
                        editedDueDate = null,
                        editedDueTime = null,
                        editedPriority = Priority.MEDIUM,
                        editedTags = note.tags,
                        titleTouched = false,
                        bodyTouched = false,
                        dueDateTouched = false,
                        dueTimeTouched = false,
                        priorityTouched = false,
                        tagsTouched = false,
                        typeTouched = false,
                        suggestedTitle = null,
                    )
                }
            }
        }
    }

    private fun mergeEnhanced(capture: ParsedCapture, result: ParseResult, log: InferenceLog?) {
        _uiState.update { state ->
            val sameTypeMerge = when {
                state.captureType == CaptureType.TASK && capture is ParsedCapture.TaskCapture ->
                    mergeTaskEnhancement(state, capture.task, result, log)
                state.captureType == CaptureType.NOTE && capture is ParsedCapture.NoteCapture ->
                    mergeNoteEnhancement(state, capture.note, result, log)
                state.typeTouched || state.hasAnyManualEdits() ->
                    state.copy(parseResult = result, inferenceLog = log ?: state.inferenceLog)
                else -> {
                    when (capture) {
                        is ParsedCapture.TaskCapture -> state.copy(
                            captureType = CaptureType.TASK,
                            parsedTask = capture.task,
                            parseResult = result,
                            inferenceLog = log ?: state.inferenceLog,
                            editedTitle = capture.task.title,
                            editedBody = "",
                            editedDueDate = capture.task.dueDate,
                            editedDueTime = capture.task.dueTime,
                            editedPriority = capture.task.priority,
                            editedTags = capture.task.tags,
                            suggestedTitle = null,
                        )
                        is ParsedCapture.NoteCapture -> state.copy(
                            captureType = CaptureType.NOTE,
                            parsedTask = null,
                            parseResult = result,
                            inferenceLog = log ?: state.inferenceLog,
                            editedTitle = capture.note.title,
                            editedBody = capture.note.body,
                            editedDueDate = null,
                            editedDueTime = null,
                            editedPriority = Priority.MEDIUM,
                            editedTags = capture.note.tags,
                            suggestedTitle = null,
                        )
                    }
                }
            }
            sameTypeMerge
        }
    }

    private fun mergeTaskEnhancement(
        state: ParseReviewUiState,
        task: ParsedTask,
        result: ParseResult,
        log: InferenceLog?,
    ): ParseReviewUiState {
        val suggestedTitle = when {
            !state.titleTouched -> null
            task.title.trim() == state.editedTitle.trim() -> null
            else -> task.title
        }
        return state.copy(
            parsedTask = task,
            parseResult = result,
            inferenceLog = log ?: state.inferenceLog,
            editedTitle = if (state.titleTouched) state.editedTitle else task.title,
            editedDueDate = if (state.dueDateTouched) state.editedDueDate else task.dueDate,
            editedDueTime = if (state.dueTimeTouched) state.editedDueTime else task.dueTime,
            editedPriority = if (state.priorityTouched) state.editedPriority else task.priority,
            editedTags = if (state.tagsTouched) state.editedTags else task.tags,
            suggestedTitle = suggestedTitle,
        )
    }

    private fun mergeNoteEnhancement(
        state: ParseReviewUiState,
        note: ParsedNote,
        result: ParseResult,
        log: InferenceLog?,
    ): ParseReviewUiState {
        val suggestedTitle = when {
            !state.titleTouched -> null
            note.title.trim() == state.editedTitle.trim() -> null
            else -> note.title
        }
        return state.copy(
            parseResult = result,
            inferenceLog = log ?: state.inferenceLog,
            editedTitle = if (state.titleTouched) state.editedTitle else note.title,
            editedBody = if (state.bodyTouched) state.editedBody else note.body,
            editedTags = if (state.tagsTouched) state.editedTags else note.tags,
            suggestedTitle = suggestedTitle,
        )
    }

    fun toggleInferenceLog() {
        _uiState.update { it.copy(showInferenceLog = !it.showInferenceLog) }
    }

    fun retryParse() {
        parseCapture(_uiState.value.originalText)
    }

    fun onTypeChanged(type: CaptureType) {
        _uiState.update { it.copy(captureType = type, typeTouched = true) }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(editedTitle = title, titleTouched = true, suggestedTitle = null) }
    }

    fun applySuggestedTitle() {
        val suggestion = _uiState.value.suggestedTitle ?: return
        _uiState.update { it.copy(editedTitle = suggestion, titleTouched = true, suggestedTitle = null) }
    }

    fun onBodyChanged(body: String) {
        _uiState.update { it.copy(editedBody = body, bodyTouched = true) }
    }

    fun onDueDateChanged(date: LocalDate?) {
        _uiState.update { it.copy(editedDueDate = date, dueDateTouched = true) }
    }

    fun onDueTimeChanged(time: LocalTime?) {
        _uiState.update { it.copy(editedDueTime = time, dueTimeTouched = true) }
    }

    fun onPriorityChanged(priority: Priority) {
        _uiState.update { it.copy(editedPriority = priority, priorityTouched = true) }
    }

    fun onTagsChanged(tags: List<String>) {
        _uiState.update { it.copy(editedTags = tags, tagsTouched = true) }
    }

    fun save(onSaved: (SaveResult) -> Unit) {
        val state = _uiState.value
        if (state.editedTitle.isBlank()) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                when (state.captureType) {
                    CaptureType.TASK -> {
                        val finalTask = ParsedTask(
                            title = state.editedTitle,
                            dueDate = state.editedDueDate,
                            dueTime = state.editedDueTime,
                            priority = state.editedPriority,
                            tags = state.editedTags,
                            originalText = state.originalText,
                            confidence = state.parsedTask?.confidence ?: 0.5f,
                            suggestedEdits = null,
                        )
                        createTaskUseCase(finalTask)
                        onSaved(SaveResult.TaskSaved(state.editedDueDate))
                    }
                    CaptureType.NOTE -> {
                        val finalNote = ParsedNote(
                            title = state.editedTitle,
                            body = state.editedBody,
                            tags = state.editedTags,
                            originalText = state.originalText,
                            confidence = 0.7f,
                        )
                        createNoteUseCase(finalNote)
                        onSaved(SaveResult.NoteSaved)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}

private fun ParseReviewUiState.hasAnyManualEdits(): Boolean {
    return titleTouched || bodyTouched || dueDateTouched || dueTimeTouched || priorityTouched || tagsTouched
}
