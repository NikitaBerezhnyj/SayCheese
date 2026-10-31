package com.nikitaberezhnyj.saycheese.models

import android.net.Uri
import android.view.Surface
import com.nikitaberezhnyj.saycheese.constants.Constants

data class CameraUiState(
    val lensFacing: Int = Constants.DEFAULT_LENS_FACING,
    val flashEnabled: Boolean = Constants.DEFAULT_FLASH_ENABLED,
    val gridEnabled: Boolean = Constants.DEFAULT_GRID_ENABLED,
    val timerSeconds: Int = Constants.DEFAULT_TIMER_SECONDS,
    val photoFormat: PhotoFormat = Constants.DEFAULT_PHOTO_FORMAT,
    val speechRecognitionEnabled: Boolean = Constants.DEFAULT_SPEECH_RECOGNITION,

    val settingsVisible: Boolean = false,
    val flashVisible: Boolean = false,
    val screenFlashActive: Boolean = false,
    val screenFlash: ScreenFlashMode = ScreenFlashMode.OFF,
    val timerActive: Boolean = false,
    val lastPhotoUri: Uri? = null,
    val heardCommand: VoiceCommand? = null,
    val zoomRatio: Float = 1f,
    val deviceRotation: Int = Surface.ROTATION_0,

    val speechModelInitialized: Boolean = false,
    val speechStatusKey: SpeechStatusKey = SpeechStatusKey.LOADING,

    val howItWorksVisible: Boolean = false
)
