package com.brigadeiro.app.ui.voice

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

data class VoiceInputHandle(
    val isListening: Boolean,
    val errorMessage: String?,
    val onMicClick: () -> Unit,
    val clearError: () -> Unit,
)

private const val LISTEN_TIMEOUT_MS = 15_000L

/**
 * Live speech input backed by the platform recognizer.
 *
 * Reliability notes (why this replaced the previous version):
 * - a fresh recognizer is created per session and destroyed at the end, avoiding
 *   stale listeners and `ERROR_CLIENT`;
 * - recognition availability is checked up front;
 * - if the on-device engine cannot handle pt-BR (no language pack installed) or
 *   the platform has no recognizer, we fall back to the system voice activity,
 *   which almost always works;
 * - failures are always surfaced to the user instead of being swallowed.
 */
@Composable
fun rememberVoiceInput(
    onResult: (String) -> Unit,
): VoiceInputHandle {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var usedOnDevice by remember { mutableStateOf(false) }
    var timeout by remember { mutableStateOf<Runnable?>(null) }
    val currentOnResult by rememberUpdatedState(onResult)
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    fun cancelTimeout() {
        timeout?.let { mainHandler.removeCallbacks(it) }
        timeout = null
    }

    fun teardown() {
        cancelTimeout()
        recognizer?.let { engine ->
            runCatching { engine.cancel() }
            runCatching { engine.destroy() }
        }
        recognizer = null
        isListening = false
    }

    fun finishWithText(text: String?) {
        teardown()
        if (text.isNullOrEmpty()) {
            errorMessage = "Não consegui entender. Tente de novo ou digite os ingredientes."
        } else {
            currentOnResult(text)
        }
    }

    val systemFallback = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        teardown()
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val text = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?.trim()
        if (text.isNullOrEmpty()) {
            errorMessage = "Não consegui entender. Tente de novo ou digite os ingredientes."
        } else {
            currentOnResult(text)
        }
    }

    fun startSystemFallback() {
        teardown()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "O que você tem na cozinha?")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        val launched = runCatching { systemFallback.launch(intent) }.isSuccess
        if (!launched) {
            errorMessage = "Não consegui abrir o microfone. Você pode digitar os ingredientes."
        }
    }

    fun startListening() {
        errorMessage = null

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            startSystemFallback()
            return
        }

        val (engine, onDevice) = createRecognizer(context)
        if (engine == null) {
            startSystemFallback()
            return
        }

        usedOnDevice = onDevice
        recognizer = engine
        engine.setRecognitionListener(
            object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                }

                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit

                override fun onError(error: Int) {
                    if (usedOnDevice && isOnDeviceLanguageFailure(error)) {
                        // No pt-BR model on this device: hand over to the system UI.
                        startSystemFallback()
                        return
                    }
                    teardown()
                    errorMessage = voiceErrorMessage(error)
                }

                override fun onResults(results: Bundle?) {
                    val text = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.trim()
                    finishWithText(text)
                }

                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            },
        )

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            if (!onDevice) putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        }

        isListening = true
        val started = runCatching { engine.startListening(intent) }.isSuccess
        if (!started) {
            startSystemFallback()
            return
        }

        timeout = Runnable {
            if (isListening) {
                teardown()
                errorMessage = "Não ouvi nada. Tente novamente ou digite os ingredientes."
            }
        }.also { mainHandler.postDelayed(it, LISTEN_TIMEOUT_MS) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            startListening()
        } else {
            errorMessage = "Para usar a voz, o Brigadeiro precisa acessar seu microfone. " +
                "Você pode continuar digitando normalmente."
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            timeout?.let { mainHandler.removeCallbacks(it) }
            recognizer?.let { engine ->
                runCatching { engine.cancel() }
                runCatching { engine.destroy() }
            }
        }
    }

    return VoiceInputHandle(
        isListening = isListening,
        errorMessage = errorMessage,
        onMicClick = {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED

            when {
                !hasPermission -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                isListening -> runCatching { recognizer?.stopListening() }
                else -> startListening()
            }
        },
        clearError = { errorMessage = null },
    )
}

/**
 * Returns the recognizer to use and whether it is the on-device engine. Prefers
 * on-device when the platform offers it, otherwise the default (possibly cloud)
 * recognizer.
 */
private fun createRecognizer(context: Context): Pair<SpeechRecognizer?, Boolean> {
    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
    ) {
        val onDevice = runCatching {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        }.getOrNull()
        if (onDevice != null) return onDevice to true
    }
    val standard = runCatching { SpeechRecognizer.createSpeechRecognizer(context) }.getOrNull()
    return standard to false
}

private fun isOnDeviceLanguageFailure(error: Int): Boolean =
    error == SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ||
        error == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE

internal fun voiceErrorMessage(error: Int): String = when (error) {
    SpeechRecognizer.ERROR_NO_MATCH,
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
    -> "Não consegui entender isso. Tente falar um pouco mais devagar."

    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
        "O microfone não está liberado."

    SpeechRecognizer.ERROR_NETWORK,
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
    -> "O reconhecimento de voz está sem conexão agora. Você pode digitar os ingredientes."

    SpeechRecognizer.ERROR_AUDIO ->
        "Tive um problema com o microfone. Tente de novo ou digite os ingredientes."

    SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
    SpeechRecognizer.ERROR_CLIENT,
    -> "O microfone está ocupado. Toque de novo em instantes."

    SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
    SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE,
    -> "O reconhecimento de voz não tem o português disponível no aparelho. Você pode digitar."

    SpeechRecognizer.ERROR_SERVER ->
        "O reconhecimento de voz falhou. Tente de novo ou digite os ingredientes."

    else -> "Não consegui ouvir. Tente novamente ou digite os ingredientes."
}
