package com.nikitaberezhnyj.saycheese.constants

import android.Manifest
import androidx.camera.core.CameraSelector
import com.nikitaberezhnyj.saycheese.models.PhotoFormat

object Constants {
    const val REQUEST_CODE_PERMISSIONS = 10

    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    const val PREFS_NAME = "say_cheese_prefs"
    const val KEY_LENS_FACING = "lens_facing"
    const val KEY_FLASH_ENABLED = "flash_enabled"
    const val KEY_GRID_ENABLED = "grid_enabled"
    const val KEY_TIMER_SECONDS = "timer_seconds"
    const val KEY_SPEECH_RECOGNITION = "speech_recognition"
    const val KEY_PHOTO_FORMAT = "photo_format"
    const val KEY_SHOWN_HOW_IT_WORKS = "shown_how_it_works"

    const val DEFAULT_LENS_FACING = CameraSelector.LENS_FACING_BACK
    const val DEFAULT_FLASH_ENABLED = false
    const val DEFAULT_GRID_ENABLED = true
    const val DEFAULT_TIMER_SECONDS = 3
    const val DEFAULT_SPEECH_RECOGNITION = true

    val DEFAULT_PHOTO_FORMAT = PhotoFormat.RATIO_3_4
}