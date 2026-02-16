package com.markduenas.localmind.domain.model

sealed class ParseResult {
    data class Success(val task: ParsedTask) : ParseResult()
    data class Fallback(val task: ParsedTask, val reason: String?) : ParseResult()
    data class Error(val message: String) : ParseResult()
}
