package com.markduenas.localmind.di

import com.markduenas.localmind.data.local.DatabaseDriverFactory
import com.markduenas.localmind.platform.AudioFileProvider
import com.markduenas.localmind.platform.AudioRecorder
import com.markduenas.localmind.platform.FileSharer
import com.markduenas.localmind.platform.NotificationHelper
import com.markduenas.localmind.platform.PermissionHelper
import com.markduenas.localmind.platform.PlatformSettings
import com.markduenas.localmind.platform.SpeechRecognitionService
import com.markduenas.localmind.security.EncryptionKeyProvider
import org.koin.dsl.module

val iosModule = module {
    single { EncryptionKeyProvider() }
    single { DatabaseDriverFactory(get()) }
    single { FileSharer() }
    single { NotificationHelper() }
    single { PermissionHelper() }
    single { PlatformSettings() }
    single { AudioRecorder() }
    single { AudioFileProvider() }
    single { SpeechRecognitionService() }
}
