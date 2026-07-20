package com.markduenas.localmind.platform

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeDefault
import platform.AVFAudio.setActive
import platform.Foundation.NSError
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognizer

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class SpeechRecognitionService {
    private val _result = MutableStateFlow(SpeechResult())
    actual val result: StateFlow<SpeechResult> = _result.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    actual val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private var audioEngine: AVAudioEngine? = null
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest? = null
    private var recognitionTask: SFSpeechRecognitionTask? = null

    actual fun isAvailable(): Boolean {
        return SFSpeechRecognizer()?.isAvailable() ?: false
    }

    actual fun unavailableResult(): SpeechResult {
        return SpeechResult(isFinal = true, error = "Speech recognition isn't available on this device.")
    }

    actual fun startListening() {
        // Reset previous state
        cancel()
        _result.value = SpeechResult()

        val speechRecognizer = SFSpeechRecognizer()
        if (speechRecognizer == null || !speechRecognizer.isAvailable()) {
            _result.value = SpeechResult(error = "Speech recognition not available", isFinal = true)
            return
        }
        speechRecognizer.setSupportsOnDeviceRecognition(true)

        // Configure audio session
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
                _result.value = SpeechResult(error = "Audio session error: ${it.localizedDescription}", isFinal = true)
                return
            }

            session.setActive(true, error = err.ptr)
            err.value?.let {
                _result.value = SpeechResult(error = "Audio activation error: ${it.localizedDescription}", isFinal = true)
                return
            }
        }

        val request = SFSpeechAudioBufferRecognitionRequest()
        request.setShouldReportPartialResults(true)
        request.setRequiresOnDeviceRecognition(true)
        recognitionRequest = request

        val engine = AVAudioEngine()
        val inputNode = engine.inputNode
        val recordingFormat = inputNode.outputFormatForBus(0u)

        recognitionTask = speechRecognizer.recognitionTaskWithRequest(request) { result, error ->
            if (result != null) {
                val text = result.bestTranscription.formattedString
                val isFinal = result.isFinal()
                _result.value = SpeechResult(text = text, isFinal = isFinal)
                if (isFinal) {
                    tearDown()
                }
            }
            if (error != null && _isListening.value) {
                _result.value = SpeechResult(
                    text = _result.value.text,
                    isFinal = true,
                    error = error.localizedDescription,
                )
                tearDown()
            }
        }

        inputNode.installTapOnBus(
            bus = 0u,
            bufferSize = 1024u,
            format = recordingFormat,
        ) { buffer, _ ->
            if (buffer != null) {
                request.appendAudioPCMBuffer(buffer)
            }
        }

        memScoped {
            val err = alloc<ObjCObjectVar<NSError?>>()
            engine.startAndReturnError(err.ptr)
            err.value?.let {
                _result.value = SpeechResult(error = "Audio engine error: ${it.localizedDescription}", isFinal = true)
                tearDown()
                return
            }
        }

        audioEngine = engine
        _isListening.value = true
    }

    actual fun stopListening() {
        audioEngine?.stop()
        recognitionRequest?.endAudio()
        // The recognition task will deliver a final result via its callback
    }

    actual fun cancel() {
        recognitionTask?.cancel()
        tearDown()
    }

    private fun tearDown() {
        audioEngine?.let { engine ->
            engine.inputNode.removeTapOnBus(0u)
            engine.stop()
        }
        audioEngine = null
        recognitionRequest = null
        recognitionTask = null
        _isListening.value = false

        memScoped {
            val err = alloc<ObjCObjectVar<NSError?>>()
            AVAudioSession.sharedInstance().setActive(false, error = err.ptr)
        }
    }
}
