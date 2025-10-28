package com.example.saycheese.utils

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
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
    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    @Volatile
    private var modelReady: Boolean = false

    private val modelDirName = "vosk-model-small-en-us-0.15"
    private val modelUrl =
        "https://github.com/NikitaBerezhnyj/SayCheese/raw/main/assets/vosk-model-small-en-us-0.15.zip"

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private fun getModelDir(): File {
        return File(context.filesDir, "model/$modelDirName/$modelDirName")
    }

    fun isModelReady(): Boolean = modelReady

    private suspend fun downloadAndUnzipModel(url: String, targetDir: File) = withContext(Dispatchers.IO) {
        if (!targetDir.exists()) targetDir.mkdirs()
        Log.d("SpeechRecognizer", "Downloading model from $url")

        URL(url).openStream().use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val entryName = entry.name.substringAfter("$modelDirName/", entry.name)
                    val file = File(targetDir, entryName)
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { output -> zip.copyTo(output) }
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

            val requiredFiles = listOf(
                "am/final.mdl",
                "conf/mfcc.conf",
                "graph/HCLr.fst"
            )
            val missingFiles = requiredFiles.filter { !File(modelDir, it).exists() }
            if (missingFiles.isNotEmpty()) {
                Log.e("SpeechRecognizer", "Missing files: $missingFiles")
                throw Exception("Model files are incomplete")
            }

            model = Model(modelDir.absolutePath)
            Log.d("SpeechRecognizer", "✅ Model loaded successfully")

            modelReady = true
            Log.d("SpeechRecognizer", "✅ Model ready flag set to: $modelReady")

        } catch (e: Exception) {
            modelReady = false
            Log.e("SpeechRecognizer", "Failed to load model", e)
            throw e
        }
    }

    fun startListening(
        onCheeseHeard: () -> Unit,
        onTimerHeard: () -> Unit
    ) {
        if (!modelReady || model == null) {
            Log.e("SpeechRecognizer", "Cannot start listening — model not ready")
            return
        }

        Log.d("SpeechRecognizer", "🎤 Starting listening with model ready")

        recognizer?.close()
        recognizer = Recognizer(model, sampleRate.toFloat())

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e("SpeechRecognizer", "❌ Invalid buffer size: $bufferSize")
            return
        }

        Log.d("SpeechRecognizer", "Buffer size: $bufferSize bytes")

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("SpeechRecognizer", "❌ AudioRecord not initialized properly")
                audioRecord = null
                return
            }

            Log.d("SpeechRecognizer", "✅ AudioRecord initialized successfully")

        } catch (e: SecurityException) {
            Log.e("SpeechRecognizer", "❌ No audio permission", e)
            return
        } catch (e: Exception) {
            Log.e("SpeechRecognizer", "❌ Failed to create AudioRecord", e)
            return
        }

        try {
            audioRecord?.startRecording()
            isRecording = true
            Log.d("SpeechRecognizer", "✅ Recording started")

            Thread {
                processAudio(onCheeseHeard, onTimerHeard)
            }.start()

        } catch (e: Exception) {
            Log.e("SpeechRecognizer", "❌ Failed to start recording", e)
            audioRecord?.release()
            audioRecord = null
        }
    }

    private fun processAudio(
        onCheeseHeard: () -> Unit,
        onTimerHeard: () -> Unit
    ) {
        val buffer = ShortArray(bufferSize / 2)
        var silenceCounter = 0
        var totalBytesRead = 0

        Log.d("SpeechRecognizer", "🎧 Audio processing thread started")

        while (isRecording && audioRecord != null) {
            val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0

            if (bytesRead > 0) {
                totalBytesRead += bytesRead

                val maxAmplitude = buffer.take(bytesRead).maxOrNull()?.toInt() ?: 0

                if (maxAmplitude > 500) {
                    silenceCounter = 0
                    if (totalBytesRead % 16000 == 0) {
                        Log.d("SpeechRecognizer", "🔊 Audio detected: amplitude=$maxAmplitude")
                    }
                } else {
                    silenceCounter++
                    if (silenceCounter % 100 == 0) {
                        Log.d("SpeechRecognizer", "🔇 Silence detected")
                    }
                }

                val byteBuffer = ByteArray(bytesRead * 2)
                for (i in 0 until bytesRead) {
                    byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
                    byteBuffer[i * 2 + 1] = ((buffer[i].toInt() shr 8) and 0xFF).toByte()
                }

                val recognizerInstance = recognizer
                if (recognizerInstance != null) {
                    val finalResult = recognizerInstance.acceptWaveForm(byteBuffer, bytesRead * 2)

                    if (finalResult) {
                        val result = recognizerInstance.result
                        Log.d("SpeechRecognizer", "📝 Final result: $result")
                        processResult(result, onCheeseHeard, onTimerHeard)
                    } else {
                        val partial = recognizerInstance.partialResult
                        if (partial.contains("\"partial\" : \"\"").not()) {
                            Log.d("SpeechRecognizer", "📝 Partial: $partial")
                            processResult(partial, onCheeseHeard, onTimerHeard)
                        }
                    }
                } else {
                    Log.e("SpeechRecognizer", "❌ Recognizer is null during processing")
                    break
                }

            } else {
                Log.e("SpeechRecognizer", "❌ Error reading audio: $bytesRead")
                break
            }
        }

        Log.d("SpeechRecognizer", "🎧 Audio processing thread stopped. Total bytes: $totalBytesRead")
    }

    private fun processResult(text: String, onCheeseHeard: () -> Unit, onTimerHeard: () -> Unit) {
        val lower = text.lowercase()
        Log.d("SpeechRecognizer", "Processing text: '$text'")

        if (lower.contains("cheese")) {
            Log.d("SpeechRecognizer", "🧀 'Cheese' detected!")
            onCheeseHeard()
        }
        if (lower.contains("time") || lower.contains("timer")) {
            Log.d("SpeechRecognizer", "⏱️ 'Timer' detected!")
            onTimerHeard()
        }
    }

    fun stopListening() {
        Log.d("SpeechRecognizer", "Stopping listening...")
        isRecording = false

        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            Log.d("SpeechRecognizer", "✅ AudioRecord stopped and released")
        } catch (e: Exception) {
            Log.e("SpeechRecognizer", "Error stopping AudioRecord", e)
        }
    }

    fun release() {
        Log.d("SpeechRecognizer", "Releasing resources...")
        stopListening()
        recognizer?.close()
        recognizer = null
        Log.d("SpeechRecognizer", "✅ Resources released (model preserved)")
    }
}