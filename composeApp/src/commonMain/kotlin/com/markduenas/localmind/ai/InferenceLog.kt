package com.markduenas.localmind.ai

data class InferenceLog(
    val model: String,
    val systemPrompt: String,
    val userPrompt: String,
    val rawResponse: String?,
    val durationMs: Long,
    val method: String,
    val error: String? = null,
)
