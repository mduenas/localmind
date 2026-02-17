package com.markduenas.localmind.di

import com.markduenas.localmind.data.local.DatabaseDriverFactory
import com.markduenas.localmind.notification.SummaryWorkerFactory
import com.markduenas.localmind.platform.NotificationHelper
import com.markduenas.localmind.platform.PermissionHelper
import org.koin.dsl.module

val androidModule = module {
    single { DatabaseDriverFactory(get()) }
    single { NotificationHelper(get()) }
    single { PermissionHelper(get()) }
    single { SummaryWorkerFactory(get()) }
}
