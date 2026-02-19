package com.markduenas.localmind.platform

import android.content.Context
import android.media.MediaRecorder
import android.os.Build

actual class AudioRecorder(
    private val context: Context,
) {
    private var recorder: MediaRecorder? = null
    private var recording = false

    actual fun startRecording(outputPath: String) {
        val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        mr.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(16_000)
            setAudioChannels(1)
            setOutputFile(outputPath)
            prepare()
            start()
        }

        recorder = mr
        recording = true
    }

    actual fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {
            // Recorder may already be stopped
        }
        recorder = null
        recording = false
    }

    actual fun isRecording(): Boolean = recording
}
