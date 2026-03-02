package com.markduenas.localmind

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.work.Configuration
import com.markduenas.localmind.data.repository.BillingRepository
import com.markduenas.localmind.di.androidModule
import com.markduenas.localmind.di.appModule
import com.markduenas.localmind.notification.NotificationChannels
import com.markduenas.localmind.notification.SummaryWorkerFactory
import com.markduenas.localmind.platform.ActivityProvider
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LocalMindApplication : Application(), Configuration.Provider {

    private val workerFactory: SummaryWorkerFactory by inject()
    private val billingRepository: BillingRepository by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LocalMindApplication)
            modules(androidModule, appModule)
        }
        NotificationChannels.createAll(this)
        billingRepository.initialize()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                ActivityProvider.activity = activity
            }
            override fun onActivityPaused(activity: Activity) {
                if (ActivityProvider.activity === activity) {
                    ActivityProvider.activity = null
                }
            }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
