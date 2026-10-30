package com.nikitaberezhnyj.saycheese.controllers

import android.content.Context
import android.util.Log
import android.view.Surface
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.ZoomState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.camera.core.UseCaseGroup

class CameraController(
    private val context: Context,
    private val onZoomChanged: (Float) -> Unit
) {
    private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    private var camera: Camera? = null
    private var previewView: PreviewView? = null
    private var photoRotation = Surface.ROTATION_0
    private var zoomLiveData: LiveData<ZoomState>? = null
    private val zoomObserver = Observer<ZoomState> { onZoomChanged(it.zoomRatio) }

    var imageCapture: ImageCapture? = null
        private set

    fun bindCamera(
        view: PreviewView,
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector,
        flashEnabled: Boolean
    ) {
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(view.surfaceProvider)

                val capture = ImageCapture.Builder()
                    .setFlashMode(flashMode(flashEnabled))
                    .setTargetRotation(photoRotation)
                    .build()

                val useCaseGroup = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(capture)
                    .apply { view.viewPort?.let { setViewPort(it) } }
                    .build()

                cameraProvider.unbindAll()

                val boundCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    useCaseGroup
                )

                camera = boundCamera
                previewView = view
                imageCapture = capture
                observeZoom(boundCamera)
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun unbindCamera() {
        zoomLiveData?.removeObserver(zoomObserver)
        zoomLiveData = null
        camera = null
        previewView = null
        imageCapture = null

        try {
            if (cameraProviderFuture.isDone) {
                cameraProviderFuture.get().unbindAll()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unbind camera", e)
        }
    }

    fun setFlash(enabled: Boolean) {
        imageCapture?.flashMode = flashMode(enabled)
    }

    fun setPhotoRotation(rotation: Int) {
        photoRotation = rotation
        imageCapture?.targetRotation = rotation
    }

    fun setZoomRatio(ratio: Float) {
        val camera = camera ?: return
        val state = camera.cameraInfo.zoomState.value ?: return
        camera.cameraControl.setZoomRatio(ratio.coerceIn(state.minZoomRatio, state.maxZoomRatio))
    }

    fun zoomBy(factor: Float) {
        val state = camera?.cameraInfo?.zoomState?.value ?: return
        setZoomRatio(state.zoomRatio * factor)
    }

    fun focusAt(x: Float, y: Float) {
        val camera = camera ?: return
        val view = previewView ?: return
        val point = view.meteringPointFactory.createPoint(x, y)
        camera.cameraControl.startFocusAndMetering(FocusMeteringAction.Builder(point).build())
    }

    private fun observeZoom(camera: Camera) {
        zoomLiveData?.removeObserver(zoomObserver)
        val liveData = camera.cameraInfo.zoomState
        liveData.observeForever(zoomObserver)
        zoomLiveData = liveData
    }

    private fun flashMode(enabled: Boolean) =
        if (enabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF

    companion object {
        private const val TAG = "CameraController"
    }
}