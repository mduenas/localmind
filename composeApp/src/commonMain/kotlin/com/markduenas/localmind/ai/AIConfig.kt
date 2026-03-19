package com.markduenas.localmind.ai

object AIConfig {
    // LLM models
    const val TINY_LLM_MODEL = "gemma3-270m"
    const val FUNCTION_TINY_LLM_MODEL = "qwen3-0.6"
    const val DEFAULT_LLM_MODEL = TINY_LLM_MODEL

    // Generation parameters
    const val MAX_TOKENS_SHORT_INPUT = 96
    const val MAX_TOKENS_MEDIUM_INPUT = 144
    const val MAX_TOKENS_LONG_INPUT = 224
    const val MAX_TOKENS_RETRY = 144
    const val TEMPERATURE = 0.0
    const val CONTEXT_SIZE = 2048

    // Performance budgets
    const val LLM_TIMEOUT_MS = 8_000L

    fun timeoutMsForModel(model: String?): Long {
        val slug = model?.lowercase().orEmpty()
        return if (slug.startsWith("qwen3-")) 12_000L else LLM_TIMEOUT_MS
    }

    // Approximate download sizes for user-facing display
    val MODEL_SIZES = mapOf(
        TINY_LLM_MODEL to "~200 MB",
        FUNCTION_TINY_LLM_MODEL to "~400 MB",
    )

    // STT model slugs — no longer used (platform-native speech recognition)
    val STT_MODELS = emptySet<String>()

    // Approximate download sizes in bytes for progress estimation
    val MODEL_BYTES = mapOf(
        TINY_LLM_MODEL to 200_000_000L,
        FUNCTION_TINY_LLM_MODEL to 400_000_000L,
    )
}
