package com.markduenas.localmind.di

import com.markduenas.localmind.data.local.DatabaseDriverFactory
import com.markduenas.localmind.notification.SummaryWorkerFactory
import com.markduenas.localmind.platform.AudioFileProvider
import com.markduenas.localmind.platform.AudioRecorder
import com.markduenas.localmind.platform.FileSharer
import com.markduenas.localmind.platform.NotificationHelper
import com.markduenas.localmind.platform.PermissionHelper
import com.markduenas.localmind.platform.SpeechRecognitionService
import com.markduenas.localmind.security.EncryptionKeyProvider
import org.koin.dsl.module

val androidModule = module {
    single { EncryptionKeyProvider(get()) }
    single { DatabaseDriverFactory(get(), get()) }
    single { NotificationHelper(get()) }
    single { PermissionHelper(get()) }
    single { FileSharer(get()) }
    single { AudioRecorder(get()) }
    single { AudioFileProvider(get()) }
    single { SummaryWorkerFactory(get()) }
    single { SpeechRecognitionService(get()) }
}
