package com.markduenas.localmind.di

import com.markduenas.localmind.data.local.DatabaseDriverFactory
import com.markduenas.localmind.platform.NotificationHelper
import com.markduenas.localmind.platform.PermissionHelper
import org.koin.dsl.module

val iosModule = module {
    single { DatabaseDriverFactory() }
    single { NotificationHelper() }
    single { PermissionHelper() }
}
