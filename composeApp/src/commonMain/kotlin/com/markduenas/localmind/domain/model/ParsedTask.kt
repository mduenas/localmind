package com.markduenas.localmind.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class ParsedTask(
    val title: String,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val priority: Priority,
    val tags: List<String>,
    val originalText: String,
    val confidence: Float,
    val suggestedEdits: String?
)
