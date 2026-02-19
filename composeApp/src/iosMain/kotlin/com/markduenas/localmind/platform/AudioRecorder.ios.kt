package com.markduenas.localmind.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.setActive
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToFile
import kotlin.math.roundToInt

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class AudioRecorder {
    private var engine: AVAudioEngine? = null
    private var recording = false
    private var outputPath: String? = null
    private val pcmChunks = mutableListOf<ByteArray>()
    private var nativeSampleRate: Double = 16_000.0

    actual fun startRecording(outputPath: String) {
        this.outputPath = outputPath
        pcmChunks.clear()

        val session = AVAudioSession.sharedInstance()
        memScoped {
            val err = alloc<ObjCObjectVar<NSError?>>()
            session.setCategory(
                AVAudioSessionCategoryRecord,
                mode = AVAudioSessionModeMeasurement,
                options = 0u,
                error = err.ptr,
            )
            err.value?.let {
                throw RuntimeException("Audio session category failed: ${it.localizedDescription}")
            }

            session.setActive(true, error = err.ptr)
            err.value?.let {
                throw RuntimeException("Audio session activation failed: ${it.localizedDescription}")
            }
        }

        val audioEngine = AVAudioEngine()
        val inputNode = audioEngine.inputNode
        val hwFormat = inputNode.outputFormatForBus(0u)
        nativeSampleRate = hwFormat.sampleRate

        // Install tap using the hardware's native format (null = native)
        inputNode.installTapOnBus(
            bus = 0u,
            bufferSize = 4096u,
            format = null,
        ) { buffer, _ ->
            if (buffer == null) return@installTapOnBus
            val frameCount = buffer.frameLength.toInt()
            if (frameCount == 0) return@installTapOnBus

            // Convert float32 samples to int16 PCM bytes
            val floatData = buffer.floatChannelData ?: return@installTapOnBus
            val channel0 = floatData[0] ?: return@installTapOnBus

            val bytes = ByteArray(frameCount * 2)
            for (i in 0 until frameCount) {
                val sample = channel0[i]
                val clamped = (sample * 32767f).roundToInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                bytes[i * 2] = (clamped and 0xFF).toByte()
                bytes[i * 2 + 1] = ((clamped shr 8) and 0xFF).toByte()
            }

            pcmChunks.add(bytes)
        }

        memScoped {
            val err = alloc<ObjCObjectVar<NSError?>>()
            audioEngine.startAndReturnError(err.ptr)
            err.value?.let {
                throw RuntimeException("Audio engine start failed: ${it.localizedDescription}")
            }
        }

        engine = audioEngine
        recording = true
    }

    actual fun stopRecording() {
        engine?.inputNode?.removeTapOnBus(0u)
        engine?.stop()
        engine = null
        recording = false

        outputPath?.let { path ->
            writeWavFile(path)
        }

        memScoped {
            val err = alloc<ObjCObjectVar<NSError?>>()
            AVAudioSession.sharedInstance().setActive(false, error = err.ptr)
        }
    }

    actual fun isRecording(): Boolean = recording

    private fun writeWavFile(path: String) {
        val totalSize = pcmChunks.sumOf { it.size }
        val allPcm = ByteArray(totalSize)
        var offset = 0
        for (chunk in pcmChunks) {
            chunk.copyInto(allPcm, offset)
            offset += chunk.size
        }
        pcmChunks.clear()

        val sampleRate = nativeSampleRate.toInt()
        val channels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val dataSize = allPcm.size

        val header = ByteArray(44)
        // RIFF
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        writeIntLE(header, 4, 36 + dataSize)
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        // fmt
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        writeIntLE(header, 16, 16)
        writeShortLE(header, 20, 1) // PCM
        writeShortLE(header, 22, channels)
        writeIntLE(header, 24, sampleRate)
        writeIntLE(header, 28, byteRate)
        writeShortLE(header, 32, blockAlign)
        writeShortLE(header, 34, bitsPerSample)
        // data
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        writeIntLE(header, 40, dataSize)

        // Combine header + PCM data into a single NSData and write
        val fullFile = ByteArray(44 + dataSize)
        header.copyInto(fullFile, 0)
        allPcm.copyInto(fullFile, 44)

        fullFile.usePinned { pinned ->
            val nsData = platform.Foundation.NSData.dataWithBytes(
                pinned.addressOf(0),
                fullFile.size.toULong(),
            )
            nsData?.writeToFile(path, true)
        }
    }

    private fun writeIntLE(arr: ByteArray, offset: Int, value: Int) {
        arr[offset] = (value and 0xFF).toByte()
        arr[offset + 1] = ((value shr 8) and 0xFF).toByte()
        arr[offset + 2] = ((value shr 16) and 0xFF).toByte()
        arr[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    private fun writeShortLE(arr: ByteArray, offset: Int, value: Int) {
        arr[offset] = (value and 0xFF).toByte()
        arr[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }
}
