package com.nikitaberezhnyj.saycheese.models

import android.net.Uri
import com.nikitaberezhnyj.saycheese.constants.Constants
import com.nikitaberezhnyj.saycheese.models.SpeechStatusKey

data class CameraUiState(
    val lensFacing: Int = Constants.DEFAULT_LENS_FACING,
    val flashEnabled: Boolean = Constants.DEFAULT_FLASH_ENABLED,
    val gridEnabled: Boolean = Constants.DEFAULT_GRID_ENABLED,
    val timerSeconds: Int = Constants.DEFAULT_TIMER_SECONDS,
    val speechRecognitionEnabled: Boolean =
        Constants.DEFAULT_SPEECH_RECOGNITION,

    val settingsVisible: Boolean = false,
    val flashVisible: Boolean = false,
    val timerActive: Boolean = false,
    val lastPhotoUri: Uri? = null,

    val speechModelInitialized: Boolean = false,
    val speechStatusKey: SpeechStatusKey = SpeechStatusKey.LOADING
)
