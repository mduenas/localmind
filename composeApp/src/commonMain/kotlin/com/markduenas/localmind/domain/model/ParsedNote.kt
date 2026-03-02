package com.markduenas.localmind.domain.model

data class ParsedNote(
    val title: String,
    val body: String,
    val tags: List<String>,
    val originalText: String,
    val confidence: Float
)
