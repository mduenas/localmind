package com.markduenas.localmind.domain.model

import com.markduenas.localmind.ai.InferenceLog

sealed class ParseResult {
    data class Success(val capture: ParsedCapture, val inferenceLog: InferenceLog? = null) : ParseResult()
    data class Fallback(val capture: ParsedCapture, val reason: String?, val inferenceLog: InferenceLog? = null) : ParseResult()
    data class Error(val message: String) : ParseResult()
}
