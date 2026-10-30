@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.screens

import androidx.camera.core.CameraSelector
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nikitaberezhnyj.saycheese.components.*
import com.nikitaberezhnyj.saycheese.viewmodels.CameraViewModel
import com.nikitaberezhnyj.saycheese.theme.BackgroundDark
import com.nikitaberezhnyj.saycheese.theme.Spacing
import com.nikitaberezhnyj.saycheese.theme.TextPrimary

@Composable
fun CameraScreen(
    viewModel: CameraViewModel = viewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(
                    top = WindowInsets.statusBars
                        .asPaddingValues()
                        .calculateTopPadding(),
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
        ) {
            TopBar(
                flashEnabled = uiState.flashEnabled,
                onFlashToggle = viewModel::toggleFlash,
                onTimerClick = viewModel::startTimerPhoto,
                onSettingsClick = { viewModel.setSettingsVisible(true) }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    cameraSelector = cameraSelector,
                    flashEnabled = uiState.flashEnabled,
                    onImageCaptureReady = viewModel::setImageCapture
                )

                if (uiState.gridEnabled) {
                    GridOverlay()
                }

                if (uiState.flashVisible) {
                    FlashOverlay { viewModel.hideFlash() }
                }

                if (uiState.timerActive) {
                    TimerCounter(
                        uiState.timerSeconds,
                        viewModel::onTimerFinish,
                        Modifier.align(Alignment.Center)
                    )
                }

                if (uiState.speechRecognitionEnabled) {
                    SpeechHint(
                        statusKey = uiState.speechStatusKey,
                        heardCommand = uiState.heardCommand,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = Spacing.M)
                    )
                }
            }

            BottomBar(
                lastPhotoUri = uiState.lastPhotoUri,
                onTakePhoto = viewModel::takePicture,
                onSwitchCamera = viewModel::switchCamera
            )
        }
    }
}
