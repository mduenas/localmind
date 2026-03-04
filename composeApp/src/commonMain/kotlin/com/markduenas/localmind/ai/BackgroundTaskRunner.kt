package com.markduenas.localmind.ai

expect class BackgroundTaskRunner {
    suspend fun <T> runInBackground(block: suspend () -> T): T
}
