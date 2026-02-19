package com.markduenas.localmind.platform

expect class AudioFileProvider {
    fun createTempAudioFile(): String
    fun deleteFile(path: String)
}
