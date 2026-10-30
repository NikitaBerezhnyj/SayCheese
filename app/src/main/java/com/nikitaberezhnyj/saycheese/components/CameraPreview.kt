@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.components

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nikitaberezhnyj.saycheese.controllers.CameraController

@Composable
fun CameraPreview(
    controller: CameraController,
    cameraSelector: CameraSelector,
    flashEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }.also { previewView = it }
        },
        modifier = modifier
    )

    LaunchedEffect(previewView, lifecycleOwner, cameraSelector) {
        val view = previewView ?: return@LaunchedEffect
        controller.bindCamera(view, lifecycleOwner, cameraSelector, flashEnabled)
    }

    LaunchedEffect(flashEnabled) {
        controller.setFlash(flashEnabled)
    }

    DisposableEffect(controller) {
        onDispose { controller.unbindCamera() }
    }
}