package com.nikitaberezhnyj.saycheese.controllers

import android.content.Context
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SpeechRecognizerController(
    private val context: Context
) {
    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var speechService: SpeechService? = null

    fun isModelReady(): Boolean = model != null

    suspend fun initModel() {
        if (model != null) return
        model = unpackModel()
    }

    private suspend fun unpackModel(): Model = suspendCancellableCoroutine { cont ->
        StorageService.unpack(
            context, MODEL_ASSET_DIR, "model",
            { cont.resume(it) },
            { cont.resumeWithException(it) }
        )
    }

    fun startListening(
        onCheeseHeard: () -> Unit,
        onTimerHeard: () -> Unit
    ): Boolean {
        val model = model ?: return false
        stopListening()

        return try {
            val recognizer = Recognizer(model, SAMPLE_RATE, GRAMMAR)
            val service = SpeechService(recognizer, SAMPLE_RATE)
            this.recognizer = recognizer
            speechService = service
            service.startListening(CommandListener(onCheeseHeard, onTimerHeard))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening", e)
            stopListening()
            false
        }
    }

    fun stopListening() {
        speechService?.cancel()
        speechService?.shutdown()
        speechService = null
        recognizer?.close()
        recognizer = null
    }

    fun release() {
        stopListening()
        model?.close()
        model = null
    }

    private class CommandListener(
        private val onCheeseHeard: () -> Unit,
        private val onTimerHeard: () -> Unit
    ) : RecognitionListener {
        private var handled = false

        override fun onPartialResult(hypothesis: String?) {
            if (!handled) handled = handle(hypothesis)
        }

        override fun onResult(hypothesis: String?) {
            if (!handled) handle(hypothesis)
            handled = false
        }

        override fun onFinalResult(hypothesis: String?) = Unit

        override fun onError(exception: Exception?) {
            Log.e(TAG, "Recognition error", exception)
        }

        override fun onTimeout() = Unit

        private fun handle(json: String?): Boolean {
            val obj = JSONObject(json ?: return false)
            val text = obj.optString("text").ifEmpty { obj.optString("partial") }
            if (text.isNotEmpty()) Log.d(TAG, "Heard: $text")

            val words = text.split(" ")
            return when {
                CHEESE in words -> {
                    onCheeseHeard(); true
                }

                TIMER in words -> {
                    onTimerHeard(); true
                }

                else -> false
            }
        }
    }

    companion object {
        private const val TAG = "SpeechRecognizer"
        private const val MODEL_ASSET_DIR = "model-en-us"
        private const val SAMPLE_RATE = 16000f
        private const val CHEESE = "cheese"
        private const val TIMER = "timer"
        private const val GRAMMAR = """["$CHEESE", "$TIMER", "[unk]"]"""
    }
}