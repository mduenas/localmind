package com.markduenas.localmind.di

import com.markduenas.localmind.data.local.DatabaseDriverFactory
import org.koin.dsl.module

val androidModule = module {
    single { DatabaseDriverFactory(get()) }
}
