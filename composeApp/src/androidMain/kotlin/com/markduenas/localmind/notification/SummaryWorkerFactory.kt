package com.markduenas.localmind.notification

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.markduenas.localmind.data.repository.TaskRepository

class SummaryWorkerFactory(
    private val taskRepository: TaskRepository,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        return when (workerClassName) {
            SummaryWorker::class.java.name -> SummaryWorker(
                appContext,
                workerParameters,
                taskRepository,
            )
            else -> null
        }
    }
}
