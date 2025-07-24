package com.example.saycheese.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast

class SpeechRecognitionManager(
    private val context: Context,
    private val onCheeseDetected: () -> Unit,
    private val onTimerDetected: () -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val TAG = "SpeechRecognizer"

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "onReadyForSpeech")
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Можна логувати рівень шуму, але не обов'язково
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            Log.d(TAG, "onBufferReceived")
        }

        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech")
            if (isListening) {
                Handler(Looper.getMainLooper()).postDelayed({ startListening() }, 500)
            }
        }

        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Помилка аудіо"
                SpeechRecognizer.ERROR_CLIENT -> "Помилка клієнта (можливо, не вистачає дозволів або API)"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Недостатньо дозволів"
                SpeechRecognizer.ERROR_NETWORK -> "Помилка мережі (для офлайн не повинна виникати)"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Таймаут мережі"
                SpeechRecognizer.ERROR_NO_MATCH -> "Немає збігів розпізнавання"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Розпізнавач зайнятий"
                SpeechRecognizer.ERROR_SERVER -> "Помилка сервера"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Таймаут мови (користувач не говорить)"
                else -> "Невідома помилка"
            }
            Log.e(TAG, "onError: $errorMessage ($error)")

            if (isListening) {
                speechRecognizer?.cancel()

                Handler(Looper.getMainLooper()).postDelayed({ startListening() }, 1000)
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val recognizedText = matches[0].lowercase()
                Log.d(TAG, "onResults: Recognized text: '$recognizedText'")
                if (recognizedText.contains("cheese")) {
                    onCheeseDetected()
                    Toast.makeText(context, "Слово 'Cheese' розпізнано!", Toast.LENGTH_SHORT).show()
                }

                if (recognizedText.contains("timer")) {
                    onTimerDetected()
                    Toast.makeText(context, "Слово 'Timer' розпізнано!", Toast.LENGTH_SHORT).show()
                }
            }

            if (isListening) {
                Handler(Looper.getMainLooper()).postDelayed({ startListening() }, 500)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val partialText = matches[0]
                Log.d(TAG, "onPartialResults: $partialText")
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            Log.d(TAG, "onEvent: $eventType")
        }
    }

    init {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Розпізнавання мови не доступне на цьому пристрої.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Speech recognition not available on this device.")
        } else {
            createSpeechRecognizer()
        }
    }

    private fun createSpeechRecognizer() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(recognitionListener)
        }
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "Speech recognition not available.")
            return
        }

        createSpeechRecognizer()

        if (isListening) {
            Log.d(TAG, "Already listening, cancelling and restarting.")
            speechRecognizer?.cancel()
        }

        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.startListening(recognizerIntent)
        isListening = true
        Log.d(TAG, "Started listening for speech.")
    }

    fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
        Log.d(TAG, "Stopped listening for speech.")
    }

    fun destroy() {
        isListening = false
        speechRecognizer?.destroy()
        speechRecognizer = null
        Log.d(TAG, "Destroyed SpeechRecognizer and released resources.")
    }
}
