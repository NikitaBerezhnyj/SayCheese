package com.example.saycheese.data

import android.content.Context
import com.example.saycheese.utils.Constants

data class CameraSettings(
    val lensFacing: Int,
    val flashEnabled: Boolean,
    val gridEnabled: Boolean,
    val timerSeconds: Int
)

class CameraSettingsManager(private val context: Context) {

    private val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    fun saveCameraSettings(lensFacing: Int, flashEnabled: Boolean, gridEnabled: Boolean, timerSeconds: Int) {
        prefs.edit()
            .putInt(Constants.KEY_LENS_FACING, lensFacing)
            .putBoolean(Constants.KEY_FLASH_ENABLED, flashEnabled)
            .putBoolean(Constants.KEY_GRID_ENABLED, gridEnabled)
            .putInt(Constants.KEY_TIMER_SECONDS, timerSeconds)
            .apply()
    }

    fun loadCameraSettings(): CameraSettings {
        val lens = prefs.getInt(Constants.KEY_LENS_FACING, Constants.DEFAULT_LENS_FACING)
        val flash = prefs.getBoolean(Constants.KEY_FLASH_ENABLED, Constants.DEFAULT_FLASH_ENABLED)
        val grid = prefs.getBoolean(Constants.KEY_GRID_ENABLED, Constants.DEFAULT_GRID_ENABLED)
        val timerSeconds = prefs.getInt(Constants.KEY_TIMER_SECONDS, Constants.DEFAULT_TIMER_SECONDS)
        return CameraSettings(lens, flash, grid, timerSeconds)
    }
}