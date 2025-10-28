package com.example.saycheese.utils

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat

fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onPhotoSaved: () -> Unit
) {
    Log.d("CameraX", "takePhoto called")

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/SayCheese")
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    Log.d("CameraX", "Starting photo capture...")
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                val errorMessage = when (exc.imageCaptureError) {
                    ImageCapture.ERROR_INVALID_CAMERA -> "Недійсна камера"
                    ImageCapture.ERROR_CAPTURE_FAILED -> "Не вдалося зробити фото"
                    ImageCapture.ERROR_FILE_IO -> "Помилка запису файлу"
                    ImageCapture.ERROR_CAMERA_CLOSED -> "Камера закрита"
                    ImageCapture.ERROR_UNKNOWN -> "Невідома помилка"
                    else -> "Помилка фотографування"
                }
                Log.e("CameraX", "Помилка збереження фото: $errorMessage (${exc.imageCaptureError}): ${exc.message}", exc)
                Toast.makeText(context, "Помилка: $errorMessage", Toast.LENGTH_LONG).show()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Log.d("CameraX", "✅ Фото збережено в галерею: ${output.savedUri}")
                Toast.makeText(context, "Фото збережено", Toast.LENGTH_SHORT).show()
                onPhotoSaved()
            }
        }
    )
}