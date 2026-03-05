package com.markduenas.localmind.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.UIKit.UIApplication
import platform.UIKit.UIBackgroundTaskInvalid

actual class BackgroundTaskRunner {
    actual suspend fun <T> runInBackground(block: suspend () -> T): T {
        // Begin/end background task on the main thread (UIKit requirement)
        var taskId = UIBackgroundTaskInvalid
        withContext(Dispatchers.Main) {
            taskId = UIApplication.sharedApplication.beginBackgroundTaskWithName("ModelDownload") {
                UIApplication.sharedApplication.endBackgroundTask(taskId)
            }
        }
        return try {
            block()
        } finally {
            val id = taskId
            if (id != UIBackgroundTaskInvalid) {
                withContext(Dispatchers.Main) {
                    UIApplication.sharedApplication.endBackgroundTask(id)
                }
            }
        }
    }
}
