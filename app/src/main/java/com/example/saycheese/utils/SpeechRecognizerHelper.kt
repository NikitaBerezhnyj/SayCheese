package com.example.saycheese.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.SpeechService
import org.vosk.android.RecognitionListener
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream

class SpeechRecognizerHelper(
    private val context: Context
) {
    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var speechService: SpeechService? = null

    private val modelDirName = "vosk-model-small-en-us-0.15"
    private val modelUrl =
        "https://github.com/NikitaBerezhnyj/SayCheese/raw/main/assets/vosk-model-small-en-us-0.15.zip"

    private fun getModelDir(): File {
        return File(context.filesDir, "model/$modelDirName")
    }

    // Завантажуємо zip з GitHub та розпаковуємо
    private suspend fun downloadAndUnzipModel(url: String, targetDir: File) = withContext(Dispatchers.IO) {
        if (!targetDir.exists()) targetDir.mkdirs()

        Log.d("SpeechRecognizer", "Downloading model from $url")

        URL(url).openStream().use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val file = File(targetDir, entry.name)
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { output ->
                            zip.copyTo(output)
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }

        Log.d("SpeechRecognizer", "Model downloaded and unpacked to: ${targetDir.absolutePath}")
    }

    suspend fun initModel() = withContext(Dispatchers.IO) {
        try {
            val modelDir = getModelDir()

            if (!modelDir.exists() || modelDir.listFiles()?.isEmpty() != false) {
                downloadAndUnzipModel(modelUrl, modelDir)
            }

            // Перевірка ключових файлів
            val requiredFiles = listOf("am/final.mdl", "conf/mfcc.conf", "graph/HCLG.fst")
            val missingFiles = requiredFiles.filter { !File(modelDir, it).exists() }
            if (missingFiles.isNotEmpty()) {
                Log.e("SpeechRecognizer", "Missing files: $missingFiles")
                throw Exception("Model files are incomplete")
            }

            model = Model(modelDir.absolutePath)
            Log.d("SpeechRecognizer", "Model loaded successfully from: ${modelDir.absolutePath}")

        } catch (e: Exception) {
            Log.e("SpeechRecognizer", "Failed to load model", e)
            throw e
        }
    }

    fun startListening(
        onCheeseHeard: () -> Unit,
        onTimerHeard: () -> Unit
    ) {
        if (model == null) {
            Log.e("SpeechRecognizer", "Cannot start listening - model is null")
            return
        }

        val sampleRate = 16000.0f
        if (recognizer == null) recognizer = Recognizer(model, sampleRate)
        if (speechService == null) speechService = SpeechService(recognizer, sampleRate)

        speechService?.startListening(object : RecognitionListener {
            override fun onPartialResult(hypothesis: String?) {
                hypothesis?.let { processResult(it, onCheeseHeard, onTimerHeard) }
            }

            override fun onResult(hypothesis: String?) {
                hypothesis?.let { processResult(it, onCheeseHeard, onTimerHeard) }
            }

            override fun onFinalResult(hypothesis: String?) {}
            override fun onError(exception: Exception?) { Log.e("SpeechRecognizer", "Error", exception) }
            override fun onTimeout() {}
        })
    }

    private fun processResult(text: String, onCheeseHeard: () -> Unit, onTimerHeard: () -> Unit) {
        val lower = text.lowercase()
        if (lower.contains("cheese")) onCheeseHeard()
        if (lower.contains("time") || lower.contains("timer")) onTimerHeard()
    }

    fun stopListening() {
        speechService?.stop()
        speechService?.shutdown()
        speechService = null
    }

    fun release() {
        stopListening()
        recognizer?.close()
        recognizer = null
        model?.close()
        model = null
    }
}
