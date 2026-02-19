package com.markduenas.localmind.platform

expect class AudioRecorder {
    fun startRecording(outputPath: String)
    fun stopRecording()
    fun isRecording(): Boolean
}
