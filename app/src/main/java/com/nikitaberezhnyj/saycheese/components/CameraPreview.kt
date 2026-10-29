@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.components

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nikitaberezhnyj.saycheese.controllers.CameraController

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector,
    flashEnabled: Boolean,
    onImageCaptureReady: (ImageCapture?) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember(context) {
        CameraController(context)
    }

    val currentCallback = rememberUpdatedState(onImageCaptureReady)
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }.also {
                previewView = it
            }
        },
        update = {},
        modifier = modifier
    )

    LaunchedEffect(
        previewView,
        lifecycleOwner,
        cameraSelector,
        flashEnabled
    ) {
        val view = previewView ?: return@LaunchedEffect

        cameraController.bindCamera(
            previewView = view,
            lifecycleOwner = lifecycleOwner,
            cameraSelector = cameraSelector,
            flashEnabled = flashEnabled,
            onImageCaptureReady = { capture ->
                currentCallback.value(capture)
            }
        )
    }

    DisposableEffect(cameraController) {
        onDispose {
            cameraController.unbindCamera()
            currentCallback.value(null)
        }
    }
}
