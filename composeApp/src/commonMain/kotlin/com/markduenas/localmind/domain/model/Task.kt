package com.markduenas.localmind.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class Task(
    val id: String,
    val title: String,
    val originalText: String,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val priority: Priority,
    val status: TaskStatus,
    val tags: List<Tag>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val completedAt: Instant?,
    val parsingConfidence: Float?
)
