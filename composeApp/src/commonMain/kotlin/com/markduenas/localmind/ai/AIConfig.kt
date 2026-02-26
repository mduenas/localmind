package com.markduenas.localmind.ai

object AIConfig {
    // LLM models
    const val DEFAULT_LLM_MODEL = "qwen3-0.6"
    const val FALLBACK_LLM_MODEL = "gemma3-270m"

    // Generation parameters
    const val MAX_TOKENS = 256
    const val TEMPERATURE = 0.2
    const val CONTEXT_SIZE = 1024

    // Performance budgets
    const val LLM_TIMEOUT_MS = 10_000L

    // Approximate download sizes for user-facing display
    val MODEL_SIZES = mapOf(
        DEFAULT_LLM_MODEL to "~400 MB",
        FALLBACK_LLM_MODEL to "~200 MB",
    )

    // STT model slugs — no longer used (platform-native speech recognition)
    val STT_MODELS = emptySet<String>()

    // Approximate download sizes in bytes for progress estimation
    val MODEL_BYTES = mapOf(
        DEFAULT_LLM_MODEL to 400_000_000L,
        FALLBACK_LLM_MODEL to 200_000_000L,
    )
}
