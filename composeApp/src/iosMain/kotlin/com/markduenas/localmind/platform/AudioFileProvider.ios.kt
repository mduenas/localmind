package com.markduenas.localmind.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

@OptIn(ExperimentalForeignApi::class)
actual class AudioFileProvider {
    actual fun createTempAudioFile(): String {
        val timestamp = (NSDate().timeIntervalSince1970 * 1000).toLong()
        return NSTemporaryDirectory() + "audio_${timestamp}.wav"
    }

    actual fun deleteFile(path: String) {
        try {
            NSFileManager.defaultManager.removeItemAtPath(path, error = null)
        } catch (_: Exception) {
            // Best-effort cleanup
        }
    }
}
