package com.markduenas.localmind.ai

import kotlin.test.Test
import kotlin.test.assertEquals

class AIConfigTest {

    @Test
    fun usesDefaultTimeoutForGemmaModel() {
        assertEquals(20_000L, AIConfig.timeoutMsForModel("gemma3-270m"))
    }

    @Test
    fun usesLargerTimeoutForQwenAndLlamaModels() {
        assertEquals(24_000L, AIConfig.timeoutMsForModel("qwen3-0.6"))
        assertEquals(24_000L, AIConfig.timeoutMsForModel("qwen2.5-1.5b"))
        assertEquals(24_000L, AIConfig.timeoutMsForModel("llama3.2-1b"))
    }

    @Test
    fun fallsBackToDefaultTimeoutWhenModelUnknown() {
        assertEquals(20_000L, AIConfig.timeoutMsForModel(null))
        assertEquals(20_000L, AIConfig.timeoutMsForModel("unknown-model"))
    }
}
