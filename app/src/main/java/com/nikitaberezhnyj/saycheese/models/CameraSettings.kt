package com.nikitaberezhnyj.saycheese.models

data class CameraSettings(
    val lensFacing: Int,
    val flashEnabled: Boolean,
    val gridEnabled: Boolean,
    val timerSeconds: Int,
    val speechRecognitionEnabled: Boolean,
    val photoFormat: PhotoFormat
)