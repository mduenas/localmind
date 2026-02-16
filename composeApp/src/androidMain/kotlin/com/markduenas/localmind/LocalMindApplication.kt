package com.markduenas.localmind

import android.app.Application
import com.markduenas.localmind.di.androidModule
import com.markduenas.localmind.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LocalMindApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LocalMindApplication)
            modules(androidModule, appModule)
        }
    }
}
