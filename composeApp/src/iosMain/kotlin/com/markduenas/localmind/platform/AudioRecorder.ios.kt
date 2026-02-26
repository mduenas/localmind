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
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeDefault
import platform.AVFAudio.setActive
import platform.Foundation.NSError
import platform.Foundation.NSMutableData
import platform.Foundation.appendBytes
import platform.Foundation.writeToFile
import kotlin.math.roundToInt

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class AudioRecorder {
    private var engine: AVAudioEngine? = null
    private var recording = false
    private var outputPath: String? = null
    private var hardwareSampleRate: Double = 48000.0
    private val pcmChunks = mutableListOf<ByteArray>()

    actual fun startRecording(outputPath: String) {
        this.outputPath = outputPath
        pcmChunks.clear()

        val session = AVAudioSession.sharedInstance()
        memScoped {
            val err = alloc<ObjCObjectVar<NSError?>>()
            session.setCategory(
                AVAudioSessionCategoryPlayAndRecord,
                mode = AVAudioSessionModeDefault,
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

        // Use the input node's native output format for the tap.
        // The engine provides Float32 non-interleaved data.
        val tapFormat = inputNode.outputFormatForBus(0u)
        hardwareSampleRate = tapFormat.sampleRate
        println("[AudioRecorder] Tap format: sampleRate=${tapFormat.sampleRate}, channels=${tapFormat.channelCount}, interleaved=${tapFormat.isInterleaved()}")

        inputNode.installTapOnBus(
            bus = 0u,
            bufferSize = 8192u,
            format = tapFormat,
        ) { buffer, _ ->
            if (buffer == null) return@installTapOnBus
            val frameCount = buffer.frameLength.toInt()
            if (frameCount == 0) return@installTapOnBus

            val floatData = buffer.floatChannelData
            if (floatData == null) {
                println("[AudioRecorder] WARNING: floatChannelData is null, frameCount=$frameCount")
                return@installTapOnBus
            }
            val channel0 = floatData[0]
            if (channel0 == null) {
                println("[AudioRecorder] WARNING: channel0 is null")
                return@installTapOnBus
            }

            // Convert float32 samples to int16 little-endian PCM
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
        println("[AudioRecorder] Recording started to: $outputPath")
    }

    actual fun stopRecording() {
        engine?.inputNode?.removeTapOnBus(0u)
        engine?.stop()
        engine = null
        recording = false

        println("[AudioRecorder] Recording stopped. Chunks captured: ${pcmChunks.size}")

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
        println("[AudioRecorder] Writing WAV: totalPcmBytes=$totalSize, hwRate=$hardwareSampleRate, path=$path")

        if (totalSize == 0) {
            println("[AudioRecorder] WARNING: No audio data captured!")
        }

        val allPcm = ByteArray(totalSize)
        var offset = 0
        for (chunk in pcmChunks) {
            chunk.copyInto(allPcm, offset)
            offset += chunk.size
        }
        pcmChunks.clear()

        // Resample from hardware rate to 16kHz for Whisper
        val targetRate = 16_000
        val pcmData = resamplePcm16(allPcm, hardwareSampleRate.toInt(), targetRate)
        println("[AudioRecorder] Resampled: ${allPcm.size / 2} samples -> ${pcmData.size / 2} samples")

        val sampleRate = targetRate
        val channels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val dataSize = pcmData.size

        // Build complete WAV file: 44-byte header + PCM data
        val fullFile = ByteArray(44 + dataSize)

        // RIFF header
        writeString(fullFile, 0, "RIFF")
        writeIntLE(fullFile, 4, 36 + dataSize)
        writeString(fullFile, 8, "WAVE")

        // fmt sub-chunk
        writeString(fullFile, 12, "fmt ")
        writeIntLE(fullFile, 16, 16) // sub-chunk size
        writeShortLE(fullFile, 20, 1) // PCM format
        writeShortLE(fullFile, 22, channels)
        writeIntLE(fullFile, 24, sampleRate)
        writeIntLE(fullFile, 28, byteRate)
        writeShortLE(fullFile, 32, blockAlign)
        writeShortLE(fullFile, 34, bitsPerSample)

        // data sub-chunk
        writeString(fullFile, 36, "data")
        writeIntLE(fullFile, 40, dataSize)

        // PCM audio data
        pcmData.copyInto(fullFile, 44)

        // Write to file
        fullFile.usePinned { pinned ->
            val nsData = platform.Foundation.NSMutableData().apply {
                appendBytes(pinned.addressOf(0), fullFile.size.toULong())
            }
            val written = nsData.writeToFile(path, true)
            println("[AudioRecorder] WAV file written: success=$written, size=${fullFile.size} bytes")
        }
    }

    /**
     * Resample 16-bit little-endian PCM from [srcRate] to [dstRate] using linear interpolation.
     */
    private fun resamplePcm16(input: ByteArray, srcRate: Int, dstRate: Int): ByteArray {
        if (srcRate == dstRate) return input

        val srcSamples = input.size / 2
        val dstSamples = ((srcSamples.toLong() * dstRate) / srcRate).toInt()
        val output = ByteArray(dstSamples * 2)
        val ratio = srcRate.toDouble() / dstRate.toDouble()

        for (i in 0 until dstSamples) {
            val srcPos = i * ratio
            val srcIdx = srcPos.toInt()
            val frac = srcPos - srcIdx

            val s0 = readSampleLE(input, srcIdx.coerceAtMost(srcSamples - 1))
            val s1 = readSampleLE(input, (srcIdx + 1).coerceAtMost(srcSamples - 1))
            val interpolated = (s0 + frac * (s1 - s0)).roundToInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())

            output[i * 2] = (interpolated and 0xFF).toByte()
            output[i * 2 + 1] = ((interpolated shr 8) and 0xFF).toByte()
        }
        return output
    }

    private fun readSampleLE(arr: ByteArray, sampleIndex: Int): Int {
        val offset = sampleIndex * 2
        val lo = arr[offset].toInt() and 0xFF
        val hi = arr[offset + 1].toInt()
        return (hi shl 8) or lo
    }

    private fun writeString(arr: ByteArray, offset: Int, s: String) {
        for (i in s.indices) {
            arr[offset + i] = s[i].code.toByte()
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
