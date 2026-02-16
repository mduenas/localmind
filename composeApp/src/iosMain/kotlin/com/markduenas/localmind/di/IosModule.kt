package com.markduenas.localmind.di

import com.markduenas.localmind.data.local.DatabaseDriverFactory
import org.koin.dsl.module

val iosModule = module {
    single { DatabaseDriverFactory() }
}
