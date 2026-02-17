package com.markduenas.localmind

import android.app.Application
import androidx.work.Configuration
import com.markduenas.localmind.di.androidModule
import com.markduenas.localmind.di.appModule
import com.markduenas.localmind.notification.NotificationChannels
import com.markduenas.localmind.notification.SummaryWorkerFactory
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LocalMindApplication : Application(), Configuration.Provider {

    private val workerFactory: SummaryWorkerFactory by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LocalMindApplication)
            modules(androidModule, appModule)
        }
        NotificationChannels.createAll(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
