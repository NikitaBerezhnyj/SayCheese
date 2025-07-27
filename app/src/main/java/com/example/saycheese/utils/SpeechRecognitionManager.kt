package com.example.saycheese.utils

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONObject
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import java.io.*
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class VoskSpeechRecognizer(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onPartialResult: (String) -> Unit = {},
    private val onError: (String) -> Unit = {}
) {

    companion object {
        private const val TAG = "VoskSpeechRecognizer"
        private const val SAMPLE_RATE = 16000
        private const val MODEL_NAME = "vosk-model-small-en-us-0.15"
    }

    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recognitionJob: Job? = null

    init {
        LibVosk.setLogLevel(LogLevel.INFO)
    }

    suspend fun initializeModel(): Boolean = withContext(Dispatchers.IO) {
        try {
            val modelPath = copyModelToInternalStorage()
            if (modelPath == null) {
                onError("Не вдалося скопіювати модель")
                return@withContext false
            }

            model = Model(modelPath)
            recognizer = Recognizer(model, SAMPLE_RATE.toFloat())

            Log.d(TAG, "Модель успішно ініціалізована")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Помилка ініціалізації моделі", e)
            onError("Помилка ініціалізації: ${e.message}")
            false
        }
    }

    private fun copyModelToInternalStorage(): String? {
        return try {
            val modelDir = File(context.filesDir, MODEL_NAME)

            if (modelDir.exists()) {
                return modelDir.absolutePath
            }

            modelDir.mkdirs()

            val assetManager = context.assets
            val modelAssetPath = "models/$MODEL_NAME"

            copyAssetFolder(assetManager, modelAssetPath, modelDir.absolutePath)

            modelDir.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Помилка копіювання моделі", e)
            null
        }
    }

    private fun copyAssetFolder(assetManager: android.content.res.AssetManager, fromAssetPath: String, toPath: String) {
        try {
            val files = assetManager.list(fromAssetPath) ?: return

            if (files.isEmpty()) {
                copyAssetFile(assetManager, fromAssetPath, toPath)
            } else {
                val folder = File(toPath)
                if (!folder.exists()) {
                    folder.mkdirs()
                }

                for (file in files) {
                    copyAssetFolder(
                        assetManager,
                        "$fromAssetPath/$file",
                        "$toPath/$file"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Помилка копіювання папки: $fromAssetPath", e)
        }
    }

    private fun copyAssetFile(assetManager: android.content.res.AssetManager, fromAssetPath: String, toPath: String) {
        try {
            assetManager.open(fromAssetPath).use { inputStream ->
                FileOutputStream(toPath).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Помилка копіювання файлу: $fromAssetPath", e)
        }
    }

    fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startRecognition() {
        if (!hasAudioPermission()) {
            onError("Немає дозволу на використання мікрофона")
            return
        }

        if (isRecording) {
            Log.w(TAG, "Розпізнавання вже триває")
            return
        }

        if (recognizer == null) {
            onError("Модель не ініціалізована")
            return
        }

        try {
            val bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            // Додатково обробляємо SecurityException при створенні AudioRecord
            audioRecord = try {
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
            } catch (se: SecurityException) {
                onError("Немає дозволу на використання мікрофона (SecurityException)")
                return
            }

            val audioRecordState = audioRecord?.state ?: AudioRecord.STATE_UNINITIALIZED
            if (audioRecordState != AudioRecord.STATE_INITIALIZED) {
                onError("Не вдалося ініціалізувати AudioRecord")
                return
            }

            isRecording = true

            audioRecord?.startRecording()

            recognitionJob = CoroutineScope(Dispatchers.IO).launch {
                processAudio()
            }

            Log.d(TAG, "Розпізнавання розпочато")

        } catch (e: Exception) {
            Log.e(TAG, "Помилка початку розпізнавання", e)
            onError("Помилка початку розпізнавання: ${e.message}")
        }
    }

    fun stopRecognition() {
        isRecording = false
        recognitionJob?.cancel()

        audioRecord?.apply {
            val currentState = state
            if (currentState == AudioRecord.STATE_INITIALIZED) {
                try {
                    stop()
                } catch (e: IllegalStateException) {
                    Log.w(TAG, "AudioRecord вже зупинено", e)
                }
            }
            release()
        }
        audioRecord = null

        recognizer?.let { rec ->
            val finalResult = rec.finalResult
            if (finalResult.isNotEmpty()) {
                try {
                    val jsonResult = JSONObject(finalResult)
                    val text = jsonResult.optString("text", "")
                    if (text.isNotEmpty()) {
                        onResult(text)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Помилка парсингу фінального результату", e)
                }
            }
        }

        Log.d(TAG, "Розпізнавання зупинено")
    }

    private suspend fun processAudio() = withContext(Dispatchers.IO) {
        val buffer = ByteArray(4096)

        while (isRecording && audioRecord != null) {
            val currentAudioRecord = audioRecord
            if (currentAudioRecord == null) break

            val bytesRead = try {
                currentAudioRecord.read(buffer, 0, buffer.size)
            } catch (e: SecurityException) {
                Log.e(TAG, "Втрачено дозвіл на мікрофон", e)
                withContext(Dispatchers.Main) {
                    onError("Втрачено дозвіл на мікрофон")
                }
                break
            } catch (e: Exception) {
                Log.e(TAG, "Помилка читання аудіо", e)
                break
            }

            if (bytesRead > 0) {
                recognizer?.let { rec ->
                    val acceptResult = try {
                        rec.acceptWaveForm(buffer, bytesRead)
                    } catch (e: Exception) {
                        Log.e(TAG, "Помилка обробки аудіо", e)
                        false
                    }

                    if (acceptResult) {
                        val result = rec.result
                        try {
                            val jsonResult = JSONObject(result)
                            val text = jsonResult.optString("text", "")
                            if (text.isNotEmpty()) {
                                withContext(Dispatchers.Main) {
                                    onResult(text)
                                }
                            } else {
                                Unit
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Помилка парсингу результату", e)
                        }
                    } else {
                        val partialResult = rec.partialResult
                        try {
                            val jsonResult = JSONObject(partialResult)
                            val partial = jsonResult.optString("partial", "")
                            if (partial.isNotEmpty()) {
                                withContext(Dispatchers.Main) {
                                    onPartialResult(partial)
                                }
                            } else {
                                Unit
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Помилка парсингу часткового результату", e)
                        }
                    }
                }
            }
        }
    }

    fun cleanup() {
        stopRecognition()
        recognizer?.close()
        model?.close()
        recognizer = null
        model = null
    }
}