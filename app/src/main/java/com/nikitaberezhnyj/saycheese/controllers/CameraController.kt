package com.nikitaberezhnyj.saycheese.controllers

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

class CameraController(
    private val context: Context
) {
    private val cameraProviderFuture =
        ProcessCameraProvider.getInstance(context)

    fun bindCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector,
        flashEnabled: Boolean,
        onImageCaptureReady: (ImageCapture) -> Unit
    ) {
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build()
                val imageCapture = ImageCapture.Builder()
                    .setFlashMode(
                        if (flashEnabled) {
                            ImageCapture.FLASH_MODE_ON
                        } else {
                            ImageCapture.FLASH_MODE_OFF
                        }
                    )
                    .build()

                preview.setSurfaceProvider(previewView.surfaceProvider)

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                onImageCaptureReady(imageCapture)
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Camera initialization failed",
                    e
                )
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun unbindCamera() {
        try {
            if (cameraProviderFuture.isDone) {
                cameraProviderFuture.get().unbindAll()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unbind camera", e)
        }
    }

    companion object {
        private const val TAG = "CameraController"
    }
}
