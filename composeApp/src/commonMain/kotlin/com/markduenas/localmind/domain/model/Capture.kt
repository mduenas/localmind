package com.markduenas.localmind.domain.model

import kotlinx.datetime.Instant

data class Capture(
    val id: String,
    val rawText: String,
    val audioPath: String?,
    val createdAt: Instant,
    val processed: Boolean
)
