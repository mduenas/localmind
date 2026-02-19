package com.markduenas.localmind.domain.model

import com.markduenas.localmind.ai.InferenceLog

sealed class ParseResult {
    data class Success(val task: ParsedTask, val inferenceLog: InferenceLog? = null) : ParseResult()
    data class Fallback(val task: ParsedTask, val reason: String?, val inferenceLog: InferenceLog? = null) : ParseResult()
    data class Error(val message: String) : ParseResult()
}
