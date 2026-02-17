package com.markduenas.localmind.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markduenas.localmind.domain.model.ParseResult
import com.markduenas.localmind.domain.model.ParsedTask
import com.markduenas.localmind.domain.model.Priority
import com.markduenas.localmind.domain.usecase.CreateTaskUseCase
import com.markduenas.localmind.domain.usecase.ParseCaptureUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class ParseReviewUiState(
    val isLoading: Boolean = true,
    val originalText: String = "",
    val parsedTask: ParsedTask? = null,
    val parseResult: ParseResult? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
    val editedTitle: String = "",
    val editedDueDate: LocalDate? = null,
    val editedDueTime: LocalTime? = null,
    val editedPriority: Priority = Priority.MEDIUM,
    val editedTags: List<String> = emptyList(),
)

class ParseReviewViewModel(
    private val parseCaptureUseCase: ParseCaptureUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParseReviewUiState())
    val uiState: StateFlow<ParseReviewUiState> = _uiState.asStateFlow()

    fun parseCapture(rawText: String) {
        _uiState.update { it.copy(isLoading = true, originalText = rawText, error = null) }
        viewModelScope.launch {
            val result = parseCaptureUseCase(rawText)
            when (result) {
                is ParseResult.Success -> applyParsed(result.task, result)
                is ParseResult.Fallback -> applyParsed(result.task, result)
                is ParseResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    private fun applyParsed(task: ParsedTask, result: ParseResult) {
        _uiState.update {
            it.copy(
                isLoading = false,
                parsedTask = task,
                parseResult = result,
                editedTitle = task.title,
                editedDueDate = task.dueDate,
                editedDueTime = task.dueTime,
                editedPriority = task.priority,
                editedTags = task.tags,
            )
        }
    }

    fun retryParse() {
        parseCapture(_uiState.value.originalText)
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(editedTitle = title) }
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

    fun saveTask(onSaved: () -> Unit) {
        val state = _uiState.value
        if (state.editedTitle.isBlank()) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
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
                onSaved()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
