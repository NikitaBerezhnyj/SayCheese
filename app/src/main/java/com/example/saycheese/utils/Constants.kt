package com.example.saycheese.utils

import android.Manifest
import android.os.Build
import androidx.camera.core.CameraSelector

object Constants {
    const val REQUEST_CODE_PERMISSIONS = 10
    val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    } else {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

    const val PREFS_NAME = "say_cheese_prefs"
    const val KEY_LENS_FACING = "lens_facing"
    const val KEY_FLASH_ENABLED = "flash_enabled"
    const val KEY_GRID_ENABLED = "grid_enabled"
    const val KEY_TIMER_SECONDS = "timer_seconds"
    const val KEY_SPEECH_RECOGNIZATION = "speech_recognization"
    
    const val DEFAULT_LENS_FACING = CameraSelector.LENS_FACING_BACK
    const val DEFAULT_FLASH_ENABLED = false
    const val DEFAULT_GRID_ENABLED = true
    const val DEFAULT_TIMER_SECONDS = 3
    const val DEFAULT_SPEECH_RECOGNIZATION = true

}