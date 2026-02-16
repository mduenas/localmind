package com.markduenas.localmind.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoin(platformModules: List<Module> = emptyList()): KoinApplication {
    return startKoin {
        modules(platformModules + appModule)
    }
}
