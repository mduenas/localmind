package com.markduenas.localmind.domain.model

import kotlin.time.Instant

data class Note(
    val id: String,
    val title: String,
    val body: String,
    val originalText: String,
    val tags: List<Tag>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val parsingConfidence: Float?
)
