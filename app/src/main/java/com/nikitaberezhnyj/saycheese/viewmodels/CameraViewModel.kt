package com.nikitaberezhnyj.saycheese.viewmodels

import android.app.Application
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nikitaberezhnyj.saycheese.controllers.CameraController
import com.nikitaberezhnyj.saycheese.controllers.SpeechRecognizerController
import com.nikitaberezhnyj.saycheese.data.CameraSettingsRepository
import com.nikitaberezhnyj.saycheese.models.CameraUiState
import com.nikitaberezhnyj.saycheese.models.SpeechStatusKey
import com.nikitaberezhnyj.saycheese.models.VoiceCommand
import com.nikitaberezhnyj.saycheese.utils.takePhoto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class CameraViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val settingsRepository = CameraSettingsRepository(application)
    private val speechController = SpeechRecognizerController(application)

    private val initialSettings = settingsRepository.loadCameraSettings()

    private val _uiState = MutableStateFlow(
        CameraUiState(
            lensFacing = initialSettings.lensFacing,
            flashEnabled = initialSettings.flashEnabled,
            gridEnabled = initialSettings.gridEnabled,
            timerSeconds = initialSettings.timerSeconds,
            speechRecognitionEnabled =
                initialSettings.speechRecognitionEnabled
        )
    )

    val uiState = _uiState.asStateFlow()

    val cameraController = CameraController(application) { ratio ->
        _uiState.update { it.copy(zoomRatio = ratio) }
    }

    private val orientationListener = object : OrientationEventListener(application) {
        override fun onOrientationChanged(orientation: Int) {
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return

            val rotation = when (orientation) {
                in 45..134 -> Surface.ROTATION_270
                in 135..224 -> Surface.ROTATION_180
                in 225..314 -> Surface.ROTATION_90
                else -> Surface.ROTATION_0
            }

            if (rotation != _uiState.value.deviceRotation) {
                cameraController.setPhotoRotation(rotation)
                _uiState.update { it.copy(deviceRotation = rotation) }
            }
        }
    }

    private var imageCapture: ImageCapture? = null

    private var isResumed = false
    private var isInitializingSpeech = false

    private var heardJob: Job? = null

    fun setImageCapture(capture: ImageCapture?) {
        imageCapture = capture
    }

    fun toggleFlash() {
        val enabled = !_uiState.value.flashEnabled

        settingsRepository.saveFlashEnabled(enabled)
        _uiState.update { it.copy(flashEnabled = enabled) }
    }

    fun switchCamera() {
        val state = _uiState.value

        if (state.timerActive) return

        val lensFacing =
            if (state.lensFacing ==
                CameraSelector.LENS_FACING_BACK
            ) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }

        settingsRepository.saveLensFacing(lensFacing)
        _uiState.update { it.copy(lensFacing = lensFacing) }
    }

    fun zoomBy(factor: Float) = cameraController.zoomBy(factor)

    fun resetZoom() = cameraController.setZoomRatio(1f)

    fun focusAt(x: Float, y: Float) = cameraController.focusAt(x, y)

    fun setGridEnabled(enabled: Boolean) {
        settingsRepository.saveGridEnabled(enabled)
        _uiState.update { it.copy(gridEnabled = enabled) }
    }

    fun setTimerSeconds(seconds: Int) {
        settingsRepository.saveTimerSeconds(seconds)
        _uiState.update { it.copy(timerSeconds = seconds) }
    }

    fun setSettingsVisible(visible: Boolean) {
        _uiState.update { it.copy(settingsVisible = visible) }
    }

    fun hideFlash() {
        _uiState.update { it.copy(flashVisible = false) }
    }

    fun takePicture() {
        val capture = cameraController.imageCapture ?: return

        _uiState.update { it.copy(flashVisible = true) }

        takePhoto(getApplication(), capture) { uri ->
            _uiState.update { it.copy(lastPhotoUri = uri) }
        }
    }

    fun startTimerPhoto() {
        val state = _uiState.value

        if (state.timerActive) return

        if (state.timerSeconds > 0) {
            _uiState.update { it.copy(timerActive = true) }
        } else {
            takePicture()
        }
    }

    fun onTimerFinish() {
        _uiState.update { it.copy(timerActive = false) }

        viewModelScope.launch {
            delay(100.milliseconds)
            takePicture()
        }
    }

    fun setSpeechRecognitionEnabled(enabled: Boolean) {
        settingsRepository.saveSpeechRecognitionEnabled(enabled)

        _uiState.update {
            it.copy(
                speechRecognitionEnabled = enabled,
                speechStatusKey = if (enabled) {
                    SpeechStatusKey.LOADING
                } else {
                    SpeechStatusKey.PAUSED
                }
            )
        }

        if (enabled) {
            startListeningIfReady()
        } else {
            speechController.stopListening()
        }
    }

    fun initializeSpeechModel(hasPermissions: Boolean) {
        if (!hasPermissions ||
            !_uiState.value.speechRecognitionEnabled ||
            _uiState.value.speechModelInitialized ||
            isInitializingSpeech
        ) {
            return
        }

        isInitializingSpeech = true
        _uiState.update { it.copy(speechStatusKey = SpeechStatusKey.LOADING) }

        viewModelScope.launch {
            try {
                speechController.initModel()

                _uiState.update {
                    it.copy(speechModelInitialized = true)
                }

                startListeningIfReady()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Speech model initialization failed",
                    e
                )

                _uiState.update {
                    it.copy(speechStatusKey = SpeechStatusKey.ERROR_INIT)
                }
            } finally {
                isInitializingSpeech = false
            }
        }
    }

    fun onResume() {
        isResumed = true
        if (orientationListener.canDetectOrientation()) orientationListener.enable()
        startListeningIfReady()
    }

    fun onPause() {
        isResumed = false
        orientationListener.disable()
        speechController.stopListening()

        _uiState.update {
            it.copy(speechStatusKey = SpeechStatusKey.PAUSED)
        }
    }

    private fun onCommandHeard(command: VoiceCommand) {
        heardJob?.cancel()
        _uiState.update { it.copy(heardCommand = command) }

        heardJob = viewModelScope.launch {
            delay(1200.milliseconds)
            _uiState.update { it.copy(heardCommand = null) }
        }
    }

    private fun onVoiceCommand(command: VoiceCommand) {
        if (_uiState.value.timerActive) return

        onCommandHeard(command)

        when (command) {
            VoiceCommand.CHEESE -> takePicture()
            VoiceCommand.TIMER -> startTimerPhoto()
        }
    }

    private fun startListeningIfReady() {
        val state = _uiState.value

        if (!isResumed ||
            !state.speechRecognitionEnabled ||
            !state.speechModelInitialized ||
            !speechController.isModelReady()
        ) {
            return
        }

        val started = speechController.startListening(::onVoiceCommand)

        _uiState.update {
            it.copy(
                speechStatusKey = if (started) {
                    SpeechStatusKey.LISTENING
                } else {
                    SpeechStatusKey.ERROR_INIT
                }
            )
        }
    }

    override fun onCleared() {
        speechController.release()
        imageCapture = null
        super.onCleared()
    }

    companion object {
        private const val TAG = "CameraViewModel"
    }
}
