package com.markduenas.localmind.di

import com.markduenas.localmind.ai.LLMService
import com.markduenas.localmind.ai.ModelDownloadService
import com.markduenas.localmind.ai.ModelManager
import com.markduenas.localmind.ai.RuleBasedParser
import com.markduenas.localmind.ai.TaskParser
import com.markduenas.localmind.data.local.DatabaseDriverFactory
import com.markduenas.localmind.data.local.LocalMindDb
import com.markduenas.localmind.data.repository.BillingRepository
import com.markduenas.localmind.data.repository.CaptureRepository
import com.markduenas.localmind.data.repository.CaptureRepositoryImpl
import com.markduenas.localmind.data.repository.NoteRepository
import com.markduenas.localmind.data.repository.NoteRepositoryImpl
import com.markduenas.localmind.data.repository.SettingsRepository
import com.markduenas.localmind.data.repository.TaskRepository
import com.markduenas.localmind.data.repository.TaskRepositoryImpl
import com.markduenas.localmind.domain.usecase.CompleteTaskUseCase
import com.markduenas.localmind.domain.usecase.CreateNoteUseCase
import com.markduenas.localmind.domain.usecase.CreateTaskUseCase
import com.markduenas.localmind.domain.usecase.GetTodayTasksUseCase
import com.markduenas.localmind.domain.usecase.GetUpcomingTasksUseCase
import com.markduenas.localmind.domain.usecase.ParseCaptureUseCase
import com.markduenas.localmind.ui.capture.CaptureViewModel
import com.markduenas.localmind.ui.notes.NoteListViewModel
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
    singleOf(::NoteRepositoryImpl) bind NoteRepository::class
    singleOf(::SettingsRepository)
    singleOf(::BillingRepository)

    // AI services
    singleOf(::ModelManager)
    singleOf(::ModelDownloadService)
    single { LLMService(get(), get()) }
    singleOf(::TaskParser)
    factory { RuleBasedParser() }

    // Use cases
    factoryOf(::GetTodayTasksUseCase)
    factoryOf(::GetUpcomingTasksUseCase)
    factoryOf(::CreateTaskUseCase)
    factoryOf(::CreateNoteUseCase)
    factoryOf(::CompleteTaskUseCase)
    factory {
        ParseCaptureUseCase(
            taskParser = get(),
            ruleBasedParser = get(),
            isLLMEnabled = { get<SettingsRepository>().llmEnabled.value },
            isPremium = { get<SettingsRepository>().premiumActive.value },
        )
    }

    // ViewModels
    viewModelOf(::CaptureViewModel)
    viewModelOf(::ParseReviewViewModel)
    viewModelOf(::TaskListViewModel)
    viewModelOf(::NoteListViewModel)
    viewModelOf(::SettingsViewModel)
}
