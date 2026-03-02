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
)

class ParseReviewViewModel(
    private val parseCaptureUseCase: ParseCaptureUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParseReviewUiState())
    val uiState: StateFlow<ParseReviewUiState> = _uiState.asStateFlow()

    fun parseCapture(rawText: String) {
        _uiState.update { it.copy(isLoading = true, originalText = rawText, error = null) }
        viewModelScope.launch {
            val result = parseCaptureUseCase(rawText)
            val log = when (result) {
                is ParseResult.Success -> result.inferenceLog
                is ParseResult.Fallback -> result.inferenceLog
                is ParseResult.Error -> null
            }
            when (result) {
                is ParseResult.Success -> applyParsed(result.capture, result, log)
                is ParseResult.Fallback -> applyParsed(result.capture, result, log)
                is ParseResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    private fun applyParsed(capture: ParsedCapture, result: ParseResult, log: InferenceLog?) {
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
                    )
                }
            }
        }
    }

    fun toggleInferenceLog() {
        _uiState.update { it.copy(showInferenceLog = !it.showInferenceLog) }
    }

    fun retryParse() {
        parseCapture(_uiState.value.originalText)
    }

    fun onTypeChanged(type: CaptureType) {
        _uiState.update { it.copy(captureType = type) }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(editedTitle = title) }
    }

    fun onBodyChanged(body: String) {
        _uiState.update { it.copy(editedBody = body) }
    }

    fun onDueDateChanged(date: LocalDate?) {
        _uiState.update { it.copy(editedDueDate = date) }
    }

    fun onDueTimeChanged(time: LocalTime?) {
        _uiState.update { it.copy(editedDueTime = time) }
    }

    fun onPriorityChanged(priority: Priority) {
        _uiState.update { it.copy(editedPriority = priority) }
    }

    fun onTagsChanged(tags: List<String>) {
        _uiState.update { it.copy(editedTags = tags) }
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
