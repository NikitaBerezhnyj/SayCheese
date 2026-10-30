@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.screens

import androidx.camera.core.CameraSelector
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nikitaberezhnyj.saycheese.components.*
import com.nikitaberezhnyj.saycheese.models.PhotoFormat
import com.nikitaberezhnyj.saycheese.theme.BackgroundDark
import com.nikitaberezhnyj.saycheese.theme.BarHeight
import com.nikitaberezhnyj.saycheese.theme.Spacing
import com.nikitaberezhnyj.saycheese.viewmodels.CameraViewModel

private const val FULL_SCREEN_BAR_ALPHA = 0.85f

@Composable
fun CameraScreen(
    viewModel: CameraViewModel = viewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    val iconRotation = rememberIconRotation(uiState.deviceRotation)

    val cameraSelector = remember(uiState.lensFacing) {
        CameraSelector.Builder()
            .requireLensFacing(uiState.lensFacing)
            .build()
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.onResume()
                Lifecycle.Event.ON_PAUSE -> viewModel.onPause()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.onPause()
        }
    }

    val isFull = uiState.photoFormat == PhotoFormat.FULL
    val barColor = BackgroundDark.copy(alpha = if (isFull) FULL_SCREEN_BAR_ALPHA else 1f)

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val frameArea = if (isFull) {
        Modifier
    } else {
        Modifier.padding(
            top = topInset + BarHeight.Top,
            bottom = bottomInset + BarHeight.Bottom
        )
    }
    val frameShape = uiState.photoFormat.aspectRatio
        ?.let { Modifier.aspectRatio(it) }
        ?: Modifier.fillMaxSize()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(frameArea),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = frameShape
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            focusPoint = offset
                            viewModel.focusAt(offset.x, offset.y)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ -> viewModel.zoomBy(zoom) }
                    }
            ) {
                CameraPreview(
                    controller = viewModel.cameraController,
                    cameraSelector = cameraSelector,
                    flashEnabled = uiState.flashEnabled,
                    modifier = Modifier.fillMaxSize()
                )

                if (uiState.gridEnabled) {
                    GridOverlay()
                }

                focusPoint?.let { point ->
                    FocusRing(
                        position = point,
                        onFinished = { focusPoint = null },
                        modifier = Modifier.align(Alignment.TopStart)
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .background(barColor)
                    .statusBarsPadding()
            ) {
                TopBar(
                    flashEnabled = uiState.flashEnabled,
                    iconRotation = { iconRotation.value },
                    onFlashToggle = viewModel::toggleFlash,
                    onSettingsClick = { viewModel.setSettingsVisible(true) }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.timerActive) {
                    TimerCounter(
                        uiState.timerSeconds,
                        viewModel::onTimerFinish,
                        Modifier.align(Alignment.Center)
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = Spacing.S),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.S)
                ) {
                    if (uiState.speechRecognitionEnabled) {
                        SpeechHint(
                            statusKey = uiState.speechStatusKey,
                            heardCommand = uiState.heardCommand
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .padding(horizontal = Spacing.M)
                    ) {
                        if (uiState.timerSeconds > 0) {
                            TimerButton(
                                iconRotation = { iconRotation.value },
                                onClick = viewModel::startTimerPhoto,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                        }

                        ZoomIndicator(
                            zoomRatio = uiState.zoomRatio,
                            onReset = { viewModel.setZoom(1f) },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .background(barColor)
            ) {
                BottomBar(
                    lastPhotoUri = uiState.lastPhotoUri,
                    iconRotation = { iconRotation.value },
                    onTakePhoto = viewModel::takePicture,
                    onSwitchCamera = viewModel::switchCamera
                )
            }
        }

        if (uiState.flashVisible) {
            FlashOverlay { viewModel.hideFlash() }
        }
    }
}