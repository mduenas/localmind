package com.markduenas.localmind.ai

object AIConfig {
    // LLM models
    const val TINY_LLM_MODEL = "gemma3-270m"
    const val FUNCTION_TINY_LLM_MODEL = "google/functiongemma-270m-it"
    const val DEFAULT_LLM_MODEL = TINY_LLM_MODEL

    // Generation parameters
    const val MAX_TOKENS_SHORT_INPUT = 56
    const val MAX_TOKENS_MEDIUM_INPUT = 80
    const val MAX_TOKENS_LONG_INPUT = 112
    const val TEMPERATURE = 0.2
    const val CONTEXT_SIZE = 2048

    // Performance budgets
    const val LLM_TIMEOUT_MS = 8_000L

    // Approximate download sizes for user-facing display
    val MODEL_SIZES = mapOf(
        TINY_LLM_MODEL to "~200 MB",
        FUNCTION_TINY_LLM_MODEL to "~180 MB",
    )

    // STT model slugs — no longer used (platform-native speech recognition)
    val STT_MODELS = emptySet<String>()

    // Approximate download sizes in bytes for progress estimation
    val MODEL_BYTES = mapOf(
        TINY_LLM_MODEL to 200_000_000L,
        FUNCTION_TINY_LLM_MODEL to 180_000_000L,
    )
}
