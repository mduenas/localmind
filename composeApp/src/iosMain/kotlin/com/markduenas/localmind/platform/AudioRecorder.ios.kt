package com.markduenas.localmind.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.AVFAudio.AVAudioQualityHigh
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVLinearPCMBitDepthKey
import platform.AVFAudio.AVLinearPCMIsBigEndianKey
import platform.AVFAudio.AVLinearPCMIsFloatKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatLinearPCM
import platform.Foundation.NSError
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class AudioRecorder {
    private var recorder: AVAudioRecorder? = null
    private var recording = false

    actual fun startRecording(outputPath: String) {
        val session = AVAudioSession.sharedInstance()
        memScoped {
            val sessionError = alloc<ObjCObjectVar<NSError?>>()
            session.setCategory(
                AVAudioSessionCategoryRecord,
                mode = AVAudioSessionModeMeasurement,
                options = 0u,
                error = sessionError.ptr,
            )
            sessionError.value?.let {
                throw RuntimeException("Failed to set audio session category: ${it.localizedDescription}")
            }
            session.setActive(true, error = sessionError.ptr)
            sessionError.value?.let {
                throw RuntimeException("Failed to activate audio session: ${it.localizedDescription}")
            }
        }

        val url = NSURL.fileURLWithPath(outputPath)
        val formatId = kAudioFormatLinearPCM.toLong()
        val settings: Map<Any?, Any?> = mapOf(
            AVFormatIDKey to formatId,
            AVSampleRateKey to 16_000.0,
            AVNumberOfChannelsKey to 1,
            AVLinearPCMBitDepthKey to 16,
            AVLinearPCMIsFloatKey to false,
            AVLinearPCMIsBigEndianKey to false,
            AVEncoderAudioQualityKey to AVAudioQualityHigh,
        )

        memScoped {
            val recorderError = alloc<ObjCObjectVar<NSError?>>()
            val audioRecorder = AVAudioRecorder(
                uRL = url,
                settings = settings,
                error = recorderError.ptr,
            )
            recorderError.value?.let {
                throw RuntimeException("Failed to create audio recorder: ${it.localizedDescription}")
            }

            if (!audioRecorder.prepareToRecord()) {
                throw RuntimeException("AVAudioRecorder.prepareToRecord() failed")
            }
            if (!audioRecorder.record()) {
                throw RuntimeException("AVAudioRecorder.record() failed")
            }
            recorder = audioRecorder
            recording = true
        }
    }

    actual fun stopRecording() {
        recorder?.stop()
        recorder = null
        recording = false

        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            AVAudioSession.sharedInstance().setActive(false, error = error.ptr)
        }
    }

    actual fun isRecording(): Boolean = recording
}
