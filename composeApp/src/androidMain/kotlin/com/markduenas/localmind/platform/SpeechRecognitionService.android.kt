package com.markduenas.localmind.platform

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class SpeechRecognitionService(
    private val context: Context,
) {
    private val _result = MutableStateFlow(SpeechResult())
    actual val result: StateFlow<SpeechResult> = _result.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    actual val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    /**
     * When non-null, the UI should launch this intent via an activity result launcher.
     * Used as a fallback when the SpeechRecognizer service binding fails.
     */
    val pendingRecognitionIntent = MutableStateFlow<Intent?>(null)

    private var recognizer: SpeechRecognizer? = null
    private var errorHandled = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private fun findRecognitionService(): ComponentName? {
        val intent = Intent(RecognitionService.SERVICE_INTERFACE)
        val services = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentServices(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        val service = services.firstOrNull() ?: return null
        return ComponentName(service.serviceInfo.packageName, service.serviceInfo.name)
    }

    actual fun startListening() {
        mainHandler.post {
            try {
                destroyRecognizer()
                errorHandled = false

                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        context, Manifest.permission.RECORD_AUDIO,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    _result.value = SpeechResult(
                        error = "Microphone permission not granted. Tap the mic button again to request it.",
                        isFinal = true,
                    )
                    return@post
                }

                val useOnDevice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

                recognizer = if (useOnDevice) {
                    SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                } else {
                    val serviceComponent = findRecognitionService()
                    if (serviceComponent != null) {
                        SpeechRecognizer.createSpeechRecognizer(context, serviceComponent)
                    } else {
                        _result.value = SpeechResult(
                            error = "No speech recognition service found. Install a voice input app (e.g. FUTO Voice Input).",
                            isFinal = true,
                        )
                        return@post
                    }
                }

                recognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _result.value = SpeechResult()
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}

                    override fun onError(error: Int) {
                        // Prevent error cascade — only handle the first error
                        if (errorHandled) return
                        errorHandled = true
                        destroyRecognizer()

                        // If service binding failed due to permissions, fall back to
                        // launching the recognizer as an activity (runs in its own process)
                        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ||
                            error == SpeechRecognizer.ERROR_CLIENT
                        ) {
                            launchRecognitionActivity()
                            return
                        }

                        val message = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                            else -> "Recognition error ($error)"
                        }
                        _result.value = SpeechResult(
                            text = _result.value.text,
                            isFinal = true,
                            error = message,
                        )
                        _isListening.value = false
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        _result.value = SpeechResult(text = text, isFinal = true)
                        _isListening.value = false
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: return
                        _result.value = SpeechResult(text = text, isFinal = false)
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                    )
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }

                recognizer?.startListening(intent)
            } catch (e: Exception) {
                _result.value = SpeechResult(error = "Failed to start: ${e.message}", isFinal = true)
                _isListening.value = false
            }
        }
    }

    /**
     * Fallback: launch the speech recognizer as a standalone activity.
     * The activity runs in the recognizer app's own process with its own permissions,
     * which avoids inter-process permission issues on GrapheneOS and similar ROMs.
     */
    private fun launchRecognitionActivity() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
        }
        // Signal the UI to launch this intent via an ActivityResultLauncher
        pendingRecognitionIntent.value = intent
    }

    /**
     * Called by the UI when the recognition activity returns a result.
     */
    fun onActivityResult(text: String?) {
        pendingRecognitionIntent.value = null
        if (!text.isNullOrBlank()) {
            _result.value = SpeechResult(text = text, isFinal = true)
        }
        _isListening.value = false
    }

    actual fun stopListening() {
        mainHandler.post {
            try {
                recognizer?.stopListening()
            } catch (_: Exception) {}
        }
    }

    actual fun cancel() {
        mainHandler.post {
            destroyRecognizer()
            pendingRecognitionIntent.value = null
            _isListening.value = false
        }
    }

    private fun destroyRecognizer() {
        try {
            recognizer?.cancel()
            recognizer?.destroy()
        } catch (_: Exception) {}
        recognizer = null
    }
}
