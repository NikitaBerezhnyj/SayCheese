package com.example.saycheese.ui.screens

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.saycheese.data.CameraSettingsManager
import com.example.saycheese.ui.components.*
import com.example.saycheese.utils.PermissionUtils
import com.example.saycheese.utils.SpeechRecognitionManager
import com.example.saycheese.utils.takePhoto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val settingsManager = remember { CameraSettingsManager(context) }

    val (savedLensFacing, savedFlashEnabled, savedGridEnabled, savedTimeSecond) = settingsManager.loadCameraSettings()

    var lensFacing by remember { mutableStateOf(savedLensFacing) }
    val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    var flashEnabled by remember { mutableStateOf(savedFlashEnabled) }
    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }
    var flashVisible by remember { mutableStateOf(false) }
    var gridEnabled by remember { mutableStateOf(savedGridEnabled) }
    var settingsVisible by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(savedTimeSecond) }
    var timerActive by remember { mutableStateOf(false) }

    val hasPermissions = remember(context) {
        PermissionUtils.hasCameraPermission(context) && PermissionUtils.hasAudioPermission(context)
    }

    fun takePicture() {
        Log.d("CameraX", "Taking picture now")
        val capture = imageCapture.value
        if (capture == null) {
            Log.e("CameraX", "ImageCapture is null, camera might not be ready")
            coroutineScope.launch {
                delay(200)
                if (imageCapture.value != null) {
                    takePhoto(
                        context = context,
                        imageCapture = imageCapture.value,
                        onPhotoSaved = {
                            flashVisible = true
                        }
                    )
                }
            }
            return
        }
        takePhoto(
            context = context,
            imageCapture = capture,
            onPhotoSaved = {
                flashVisible = true
            }
        )
    }

    fun startTimerPhoto() {
        if (timerSeconds > 0) {
            timerActive = true
            Log.d("TIMER_BUTTON", "Timer started for $timerSeconds seconds")
        } else {
            takePicture()
        }
    }

    fun onTimerFinish() {
        timerActive = false
        Log.d("TIMER_BUTTON", "Timer finished, taking photo in 100ms")
        coroutineScope.launch {
            delay(100)
            takePicture()
        }
    }

    val speechRecognitionManager = remember {
        SpeechRecognitionManager(
            context = context,
            onCheeseDetected = {
                Log.d("SpeechRecognition", "Cheese command detected - taking photo")
                if (!timerActive) {
                    takePicture()
                }
            },
            onTimerDetected = {
                Log.d("SpeechRecognition", "Timer command detected - starting timer photo")
                if (!timerActive) {
                    startTimerPhoto()
                }
            }
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (hasPermissions) {
                        speechRecognitionManager.startListening()
                        Log.d("SpeechRecognition", "Started speech recognition")
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    speechRecognitionManager.stopListening()
                    Log.d("SpeechRecognition", "Stopped speech recognition")
                }
                Lifecycle.Event.ON_DESTROY -> {
                    speechRecognitionManager.destroy()
                    Log.d("SpeechRecognition", "Destroyed speech recognition")
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            speechRecognitionManager.destroy()
        }
    }

    if (!hasPermissions) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Потрібен дозвіл на камеру та мікрофон",
                color = Color.White
            )
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
        ) {
            TopBar(
                flashEnabled = flashEnabled,
                onFlashToggle = {
                    flashEnabled = !flashEnabled
                    settingsManager.saveCameraSettings(lensFacing, flashEnabled, gridEnabled, timerSeconds)
                    Log.d("CameraX", "Flash toggled: $flashEnabled")
                },
                onSettingsClick = { settingsVisible = true }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    cameraSelector = cameraSelector,
                    flashEnabled = flashEnabled,
                    imageCapture = imageCapture,
                    lensFacing = lensFacing
                )

                if (gridEnabled) {
                    GridOverlay()
                }

                if (flashVisible) {
                    FlashOverlay(onAnimationEnd = { flashVisible = false })
                }

                if (timerActive) {
                    TimerCounter(
                        totalSeconds = timerSeconds,
                        onTimerFinish = ::onTimerFinish,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            BottomBar(
                imageCapture = imageCapture,
                onTakePhoto = { takePicture() },
                onTakePhotoWithTimer = { startTimerPhoto() },
                onSwitchCamera = {
                    if (!timerActive) {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                            CameraSelector.LENS_FACING_FRONT
                        else
                            CameraSelector.LENS_FACING_BACK
                        settingsManager.saveCameraSettings(
                            lensFacing,
                            flashEnabled,
                            gridEnabled,
                            timerSeconds
                        )
                        Log.d("CameraX", "Switch camera clicked, new lens: $lensFacing")
                    }
                }
            )
        }

        if (settingsVisible) {
            SettingsDialog(
                gridEnabled = gridEnabled,
                timerSeconds = timerSeconds,
                onGridChange = {
                    gridEnabled = it
                    settingsManager.saveCameraSettings(lensFacing, flashEnabled, gridEnabled, timerSeconds)
                },
                onTimerChange = {
                    timerSeconds = it
                    settingsManager.saveCameraSettings(lensFacing, flashEnabled, gridEnabled, timerSeconds)
                },
                onDismissRequest = { settingsVisible = false }
            )
        }
    }
}