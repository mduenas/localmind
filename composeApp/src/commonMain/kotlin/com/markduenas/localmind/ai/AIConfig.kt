package com.markduenas.localmind.ai

object AIConfig {
    // LLM models
    const val DEFAULT_LLM_MODEL = "qwen3-0.6"
    const val FALLBACK_LLM_MODEL = "gemma3-270m"

    // STT models
    const val DEFAULT_STT_MODEL = "whisper-tiny"

    // Generation parameters
    const val MAX_TOKENS = 256
    const val TEMPERATURE = 0.2
    const val CONTEXT_SIZE = 1024

    // Performance budgets
    const val LLM_TIMEOUT_MS = 10_000L
    const val STT_TIMEOUT_MS = 10_000L
}
