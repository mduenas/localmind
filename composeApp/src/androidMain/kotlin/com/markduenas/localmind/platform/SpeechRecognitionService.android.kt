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
    private var languageUnavailableRetryCount = 0
    private val mainHandler = Handler(Looper.getMainLooper())

    private companion object {
        private const val MAX_LANGUAGE_UNAVAILABLE_RETRIES = 1
        private const val LANGUAGE_UNAVAILABLE_RETRY_DELAY_MS = 400L
        private const val PLAY_STORE_VOICE_APP_SEARCH_URL =
            "https://play.google.com/store/search?q=voice%20input&c=apps"
        private const val SPEECH_PREFS_NAME = "speech_recognition_prefs"
        private const val KEY_MODEL_DOWNLOAD_TRIGGERED = "model_download_triggered"
    }

    /**
     * Checked before starting so a device with no working recognizer (common on
     * a fresh install with no on-device language pack and no third-party voice
     * input app) degrades gracefully instead of hitting a dead-end error.
     */
    actual fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context) || findRecognitionService() != null
    }

    actual fun unavailableResult(): SpeechResult {
        return SpeechResult(
            isFinal = true,
            error = "No voice input app is set up on this device.",
            errorActionLabel = "Get one",
            errorActionUrl = PLAY_STORE_VOICE_APP_SEARCH_URL,
        )
    }

    private fun canLaunchRecognitionActivity(): Boolean {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
        }
        return intent.resolveActivity(context.packageManager) != null
    }

    /**
     * Asks Android to download its own on-device speech model (API 33+) rather
     * than bundling one ourselves -- this fixes the root cause of a fresh
     * install having no language pack, using the OS/Google's own mechanism.
     * Fires at most once per install; the download itself is async and has no
     * completion callback via this API, so the current attempt still falls
     * back to the recognition activity below.
     */
    private fun triggerModelDownloadOnce() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val prefs = context.getSharedPreferences(SPEECH_PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_MODEL_DOWNLOAD_TRIGGERED, false)) return
        try {
            val downloadRecognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
            }
            downloadRecognizer.triggerModelDownload(intent)
            downloadRecognizer.destroy()
            prefs.edit().putBoolean(KEY_MODEL_DOWNLOAD_TRIGGERED, true).apply()
        } catch (_: Exception) {}
    }

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
        languageUnavailableRetryCount = 0
        startListeningInternal()
    }

    private fun startListeningInternal() {
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

                if (!useOnDevice) {
                    // No reliable on-device recognizer on this OS/device. Rather than
                    // bind to an arbitrary discovered RecognitionService (which has
                    // known permission/binding issues on some ROMs), hand off directly
                    // to the system's own recognition activity -- it handles its own
                    // online/offline fallback internally (verified: it retries via
                    // network recognition when the on-device model is missing), and
                    // lets the user pick whichever voice input app they've chosen.
                    if (!canLaunchRecognitionActivity()) {
                        _result.value = unavailableResult()
                        return@post
                    }
                    launchRecognitionActivity()
                    return@post
                }

                recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)

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

                        // The on-device recognizer often isn't warmed up yet on the very
                        // first call in a session and reports the language as unavailable —
                        // this is transient and normally succeeds on an immediate retry.
                        // It can also mean the on-device language pack is missing entirely
                        // (e.g. fresh install) -- ask Android to download it for next time.
                        if (error == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE) {
                            triggerModelDownloadOnce()
                            if (languageUnavailableRetryCount < MAX_LANGUAGE_UNAVAILABLE_RETRIES) {
                                languageUnavailableRetryCount++
                                mainHandler.postDelayed(
                                    { startListeningInternal() },
                                    LANGUAGE_UNAVAILABLE_RETRY_DELAY_MS,
                                )
                                return
                            }
                            // Still unavailable (e.g. on-device language pack missing) —
                            // fall back to the system voice-input activity instead of failing.
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
                            SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
                            SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT,
                            ->
                                "Voice input isn't set up on this device. Use text capture instead."
                            else -> "Voice recognition failed. Use text capture instead."
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

    /**
     * Called by the UI when launching the fallback recognition activity itself
     * fails (e.g. ActivityNotFoundException on a device with no voice-input app).
     */
    fun onActivityLaunchFailed() {
        pendingRecognitionIntent.value = null
        _result.value = SpeechResult(
            isFinal = true,
            error = "Voice input isn't available on this device. Use text capture instead.",
        )
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
