package com.markduenas.localmind.di

import com.markduenas.localmind.data.local.DatabaseDriverFactory
import com.markduenas.localmind.platform.NotificationHelper
import com.markduenas.localmind.platform.PermissionHelper
import com.markduenas.localmind.security.EncryptionKeyProvider
import org.koin.dsl.module

val iosModule = module {
    single { EncryptionKeyProvider() }
    single { DatabaseDriverFactory(get()) }
    single { NotificationHelper() }
    single { PermissionHelper() }
}
