package com.markduenas.localmind.ai

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIApplication
import kotlin.coroutines.resume

actual class BackgroundTaskRunner {
    actual suspend fun <T> runInBackground(block: suspend () -> T): T {
        val app = UIApplication.sharedApplication
        var taskId = platform.UIKit.UIBackgroundTaskInvalid
        taskId = app.beginBackgroundTaskWithName("ModelDownload") {
            app.endBackgroundTask(taskId)
        }
        return try {
            block()
        } finally {
            if (taskId != platform.UIKit.UIBackgroundTaskInvalid) {
                app.endBackgroundTask(taskId)
            }
        }
    }
}
