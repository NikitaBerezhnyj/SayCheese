package com.nikitaberezhnyj.saycheese.data

import android.content.Context
import com.nikitaberezhnyj.saycheese.constants.Constants
import com.nikitaberezhnyj.saycheese.models.CameraSettings
import com.nikitaberezhnyj.saycheese.models.PhotoFormat

class CameraSettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    fun loadCameraSettings(): CameraSettings {
        return CameraSettings(
            lensFacing = prefs.getInt(
                Constants.KEY_LENS_FACING,
                Constants.DEFAULT_LENS_FACING
            ),
            flashEnabled = prefs.getBoolean(
                Constants.KEY_FLASH_ENABLED,
                Constants.DEFAULT_FLASH_ENABLED
            ),
            gridEnabled = prefs.getBoolean(
                Constants.KEY_GRID_ENABLED,
                Constants.DEFAULT_GRID_ENABLED
            ),
            timerSeconds = prefs.getInt(
                Constants.KEY_TIMER_SECONDS,
                Constants.DEFAULT_TIMER_SECONDS
            ),
            speechRecognitionEnabled = prefs.getBoolean(
                Constants.KEY_SPEECH_RECOGNITION,
                Constants.DEFAULT_SPEECH_RECOGNITION
            ),
            photoFormat = PhotoFormat.entries.firstOrNull {
                it.name == prefs.getString(Constants.KEY_PHOTO_FORMAT, null)
            } ?: Constants.DEFAULT_PHOTO_FORMAT
        )
    }

    fun hasShownHowItWorks(): Boolean {
        return prefs.getBoolean(Constants.KEY_SHOWN_HOW_IT_WORKS, false)
    }

    fun setShownHowItWorks(shown: Boolean) {
        prefs.edit()
            .putBoolean(Constants.KEY_SHOWN_HOW_IT_WORKS, shown)
            .apply()
    }

    fun saveLensFacing(value: Int) {
        prefs.edit()
            .putInt(Constants.KEY_LENS_FACING, value)
            .apply()
    }

    fun saveFlashEnabled(value: Boolean) {
        prefs.edit()
            .putBoolean(Constants.KEY_FLASH_ENABLED, value)
            .apply()
    }

    fun saveGridEnabled(value: Boolean) {
        prefs.edit()
            .putBoolean(Constants.KEY_GRID_ENABLED, value)
            .apply()
    }

    fun saveTimerSeconds(value: Int) {
        prefs.edit()
            .putInt(Constants.KEY_TIMER_SECONDS, value)
            .apply()
    }

    fun saveSpeechRecognitionEnabled(value: Boolean) {
        prefs.edit()
            .putBoolean(Constants.KEY_SPEECH_RECOGNITION, value)
            .apply()
    }

    fun savePhotoFormat(value: PhotoFormat) {
        prefs.edit()
            .putString(Constants.KEY_PHOTO_FORMAT, value.name)
            .apply()
    }
}