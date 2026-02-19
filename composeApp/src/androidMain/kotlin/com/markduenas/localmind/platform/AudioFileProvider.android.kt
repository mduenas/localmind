package com.markduenas.localmind.platform

import android.content.Context
import java.io.File

actual class AudioFileProvider(
    private val context: Context,
) {
    actual fun createTempAudioFile(): String {
        val file = File(context.cacheDir, "audio_${System.currentTimeMillis()}.wav")
        return file.absolutePath
    }

    actual fun deleteFile(path: String) {
        try {
            File(path).delete()
        } catch (_: Exception) {
            // Best-effort cleanup
        }
    }
}
