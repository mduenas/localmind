package com.markduenas.localmind.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVLinearPCMBitDepthKey
import platform.AVFAudio.AVLinearPCMIsFloatKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatLinearPCM
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
actual class AudioRecorder {
    private var recorder: AVAudioRecorder? = null
    private var recording = false

    actual fun startRecording(outputPath: String) {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryRecord, mode = AVAudioSessionModeMeasurement, options = 0u, error = null)
        session.setActive(true, error = null)

        val url = NSURL.fileURLWithPath(outputPath)
        val settings = mapOf<Any?, Any>(
            AVSampleRateKey to 16_000.0,
            AVNumberOfChannelsKey to 1,
            AVFormatIDKey to kAudioFormatLinearPCM.toInt(),
            AVLinearPCMBitDepthKey to 16,
            AVLinearPCMIsFloatKey to false,
        )

        val audioRecorder = AVAudioRecorder(uRL = url, settings = settings, error = null)
        audioRecorder.record()
        recorder = audioRecorder
        recording = true
    }

    actual fun stopRecording() {
        recorder?.stop()
        recorder = null
        recording = false

        val session = AVAudioSession.sharedInstance()
        session.setActive(false, error = null)
    }

    actual fun isRecording(): Boolean = recording
}
