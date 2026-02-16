package com.markduenas.localmind.di

import com.markduenas.localmind.ai.LLMService
import com.markduenas.localmind.ai.ModelManager
import com.markduenas.localmind.ai.RuleBasedParser
import com.markduenas.localmind.ai.STTService
import com.markduenas.localmind.ai.TaskParser
import com.markduenas.localmind.data.local.DatabaseDriverFactory
import com.markduenas.localmind.data.local.LocalMindDb
import com.markduenas.localmind.data.repository.CaptureRepository
import com.markduenas.localmind.data.repository.CaptureRepositoryImpl
import com.markduenas.localmind.data.repository.SettingsRepository
import com.markduenas.localmind.data.repository.TaskRepository
import com.markduenas.localmind.data.repository.TaskRepositoryImpl
import com.markduenas.localmind.domain.usecase.CompleteTaskUseCase
import com.markduenas.localmind.domain.usecase.CreateTaskUseCase
import com.markduenas.localmind.domain.usecase.GetTodayTasksUseCase
import com.markduenas.localmind.domain.usecase.GetUpcomingTasksUseCase
import com.markduenas.localmind.domain.usecase.ParseCaptureUseCase
import com.markduenas.localmind.ui.capture.CaptureViewModel
import com.markduenas.localmind.ui.review.ParseReviewViewModel
import com.markduenas.localmind.ui.settings.SettingsViewModel
import com.markduenas.localmind.ui.tasks.TaskListViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    // Database
    single<LocalMindDb> {
        val driver = get<DatabaseDriverFactory>().createDriver()
        LocalMindDb(driver)
    }

    // Repositories
    singleOf(::TaskRepositoryImpl) bind TaskRepository::class
    singleOf(::CaptureRepositoryImpl) bind CaptureRepository::class
    singleOf(::SettingsRepository)

    // AI services
    singleOf(::ModelManager)
    singleOf(::LLMService)
    singleOf(::STTService)
    singleOf(::TaskParser)
    factory { RuleBasedParser() }

    // Use cases
    factoryOf(::GetTodayTasksUseCase)
    factoryOf(::GetUpcomingTasksUseCase)
    factoryOf(::CreateTaskUseCase)
    factoryOf(::CompleteTaskUseCase)
    factory { ParseCaptureUseCase(get(), get()) }

    // ViewModels
    viewModelOf(::CaptureViewModel)
    viewModelOf(::ParseReviewViewModel)
    viewModelOf(::TaskListViewModel)
    viewModelOf(::SettingsViewModel)
}
