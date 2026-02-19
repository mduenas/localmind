package com.markduenas.localmind.platform

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import kotlin.concurrent.thread

actual class AudioRecorder(
    private val context: Context,
) {
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var recording = false

    @SuppressLint("MissingPermission")
    actual fun startRecording(outputPath: String) {
        val sampleRate = 16_000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize,
        )

        audioRecord = recorder
        recording = true
        recorder.startRecording()

        recordingThread = thread {
            writeWavFile(recorder, outputPath, sampleRate, bufferSize)
        }
    }

    actual fun stopRecording() {
        recording = false
        try {
            recordingThread?.join(2000)
        } catch (_: InterruptedException) {}
        try {
            audioRecord?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        audioRecord = null
        recordingThread = null
    }

    actual fun isRecording(): Boolean = recording

    private fun writeWavFile(recorder: AudioRecord, path: String, sampleRate: Int, bufferSize: Int) {
        val file = File(path)
        val buffer = ByteArray(bufferSize)

        FileOutputStream(file).use { fos ->
            // Write placeholder WAV header (44 bytes)
            fos.write(ByteArray(44))

            var totalDataBytes = 0L
            while (recording) {
                val read = recorder.read(buffer, 0, buffer.size)
                if (read > 0) {
                    fos.write(buffer, 0, read)
                    totalDataBytes += read
                }
            }
        }

        // Go back and fill in the WAV header with correct sizes
        writeWavHeader(file, sampleRate, totalDataBytes = file.length() - 44)
    }

    private fun writeWavHeader(file: File, sampleRate: Int, totalDataBytes: Long) {
        val channels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8

        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(0)
            raf.writeBytes("RIFF")
            raf.writeIntLE((36 + totalDataBytes).toInt())
            raf.writeBytes("WAVE")
            raf.writeBytes("fmt ")
            raf.writeIntLE(16) // PCM chunk size
            raf.writeShortLE(1) // PCM format
            raf.writeShortLE(channels)
            raf.writeIntLE(sampleRate)
            raf.writeIntLE(byteRate)
            raf.writeShortLE(blockAlign)
            raf.writeShortLE(bitsPerSample)
            raf.writeBytes("data")
            raf.writeIntLE(totalDataBytes.toInt())
        }
    }

    private fun RandomAccessFile.writeIntLE(value: Int) {
        write(value and 0xFF)
        write((value shr 8) and 0xFF)
        write((value shr 16) and 0xFF)
        write((value shr 24) and 0xFF)
    }

    private fun RandomAccessFile.writeShortLE(value: Int) {
        write(value and 0xFF)
        write((value shr 8) and 0xFF)
    }
}
