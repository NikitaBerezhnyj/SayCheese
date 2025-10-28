package com.example.saycheese.utils

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import com.example.saycheese.R

fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onPhotoSaved: () -> Unit
) {
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

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                val errorMessage = when (exc.imageCaptureError) {
                    ImageCapture.ERROR_INVALID_CAMERA -> context.getString(R.string.photo_error_invalid_camera)
                    ImageCapture.ERROR_CAPTURE_FAILED -> context.getString(R.string.photo_error_capture_failed)
                    ImageCapture.ERROR_FILE_IO -> context.getString(R.string.photo_error_file_io)
                    ImageCapture.ERROR_CAMERA_CLOSED -> context.getString(R.string.photo_error_camera_closed)
                    ImageCapture.ERROR_UNKNOWN -> context.getString(R.string.photo_error_unknown)
                    else -> context.getString(R.string.photo_error_generic)
                }

                Log.e("CameraX", "Photo save failed: $errorMessage (Error code: ${exc.imageCaptureError})", exc)

                Toast.makeText(
                    context,
                    "Failed to save photo: $errorMessage",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Log.i("CameraX", "Photo successfully saved at: ${output.savedUri}")

                Toast.makeText(
                    context,
                    "Photo successfully saved",
                    Toast.LENGTH_LONG
                ).show()
                onPhotoSaved()
            }
        }
    )
}
