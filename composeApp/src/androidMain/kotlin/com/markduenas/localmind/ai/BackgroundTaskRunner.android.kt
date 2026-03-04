package com.markduenas.localmind.ai

actual class BackgroundTaskRunner {
    actual suspend fun <T> runInBackground(block: suspend () -> T): T {
        return block()
    }
}
