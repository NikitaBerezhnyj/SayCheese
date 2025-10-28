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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.saycheese.R
import com.example.saycheese.data.CameraSettingsManager
import com.example.saycheese.ui.components.*
import com.example.saycheese.utils.PermissionUtils
import com.example.saycheese.utils.SpeechRecognizerHelper
import com.example.saycheese.utils.takePhoto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SpeechStatusKey {
    INIT, LOADING, READY, ERROR_INIT, LISTENING, PAUSED, STOPPED, INACTIVE
}

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val settingsManager = remember { CameraSettingsManager(context) }

    val (savedLensFacing, savedFlashEnabled, savedGridEnabled, savedTimeSecond, savedSpeechRecognitionEnabled) =
        settingsManager.loadCameraSettings()

    var lensFacing by remember { mutableStateOf(savedLensFacing) }
    val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    var flashEnabled by remember { mutableStateOf(savedFlashEnabled) }
    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }
    var flashVisible by remember { mutableStateOf(false) }
    var gridEnabled by remember { mutableStateOf(savedGridEnabled) }
    var settingsVisible by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(savedTimeSecond) }
    var timerActive by remember { mutableStateOf(false) }
    var speechRecognitionEnabled by remember { mutableStateOf(savedSpeechRecognitionEnabled) }

    val hasPermissions = remember {
        PermissionUtils.hasCameraPermission(context) && PermissionUtils.hasAudioPermission(context)
    }

    val speechHelper = remember { SpeechRecognizerHelper(context) }
    var speechModelInitialized by remember { mutableStateOf(false) }
    var speechStatusKey by remember { mutableStateOf(SpeechStatusKey.INIT) }
    var inactiveReason by remember { mutableStateOf("") }

    fun takePicture() {
        val capture = imageCapture.value ?: return
        takePhoto(context, capture) { flashVisible = true }
    }

    fun startTimerPhoto() {
        if (timerSeconds > 0) timerActive = true else takePicture()
    }

    fun onTimerFinish() {
        timerActive = false
        coroutineScope.launch {
            delay(100)
            takePicture()
        }
    }

    LaunchedEffect(hasPermissions) {
        if (hasPermissions) {
            speechStatusKey = SpeechStatusKey.LOADING
            try {
                speechHelper.initModel()
                speechModelInitialized = true
                speechStatusKey = SpeechStatusKey.READY
            } catch (e: Exception) {
                Log.e("SpeechRecognition", "Speech model initialization failed", e)
                speechModelInitialized = false
                speechStatusKey = SpeechStatusKey.ERROR_INIT
            }
        }
    }

    DisposableEffect(lifecycleOwner, speechModelInitialized, speechRecognitionEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (speechRecognitionEnabled && speechModelInitialized && speechHelper.isModelReady()) {
                        speechHelper.startListening(
                            onCheeseHeard = { if (!timerActive) takePicture() },
                            onTimerHeard = { if (!timerActive) startTimerPhoto() }
                        )
                        speechStatusKey = SpeechStatusKey.LISTENING
                    } else {
                        inactiveReason = when {
                            !speechRecognitionEnabled -> "disabled"
                            !speechModelInitialized -> "loading"
                            !speechHelper.isModelReady() -> "not ready"
                            else -> "unknown"
                        }
                        speechStatusKey = SpeechStatusKey.INACTIVE
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    speechHelper.stopListening()
                    speechStatusKey = SpeechStatusKey.PAUSED
                }
                Lifecycle.Event.ON_DESTROY -> {
                    speechHelper.release()
                    speechStatusKey = SpeechStatusKey.STOPPED
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer); speechHelper.release() }
    }

    val speechStatusText = when (speechStatusKey) {
        SpeechStatusKey.INIT -> stringResource(R.string.speech_init)
        SpeechStatusKey.LOADING -> stringResource(R.string.speech_loading_model)
        SpeechStatusKey.READY -> stringResource(R.string.speech_ready)
        SpeechStatusKey.ERROR_INIT -> stringResource(R.string.speech_error_init)
        SpeechStatusKey.LISTENING -> stringResource(R.string.speech_listening)
        SpeechStatusKey.PAUSED -> stringResource(R.string.speech_paused)
        SpeechStatusKey.STOPPED -> stringResource(R.string.speech_stopped)
        SpeechStatusKey.INACTIVE -> stringResource(R.string.speech_inactive,
            when (inactiveReason) {
                "disabled" -> stringResource(R.string.reason_disabled)
                "loading" -> stringResource(R.string.reason_loading)
                "not ready" -> stringResource(R.string.reason_not_ready)
                else -> stringResource(R.string.reason_unknown)
            }
        )
    }

    if (!hasPermissions) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.permission_required),
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
                    settingsManager.saveCameraSettings(
                        lensFacing, flashEnabled, gridEnabled, timerSeconds, speechRecognitionEnabled
                    )
                },
                onSettingsClick = { settingsVisible = true }
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
                    flashEnabled = flashEnabled,
                    imageCapture = imageCapture,
                    lensFacing = lensFacing
                )
                if (gridEnabled) GridOverlay()
                if (flashVisible) FlashOverlay { flashVisible = false }
                if (timerActive) TimerCounter(timerSeconds, ::onTimerFinish, Modifier.align(Alignment.Center))
            }

            BottomBar(
                imageCapture = imageCapture,
                onTakePhoto = { takePicture() },
                onTakePhotoWithTimer = { startTimerPhoto() },
                onSwitchCamera = {
                    if (!timerActive) {
                        lensFacing =
                            if (lensFacing == CameraSelector.LENS_FACING_BACK)
                                CameraSelector.LENS_FACING_FRONT
                            else
                                CameraSelector.LENS_FACING_BACK
                        settingsManager.saveCameraSettings(
                            lensFacing, flashEnabled, gridEnabled, timerSeconds, speechRecognitionEnabled
                        )
                    }
                }
            )
        }

        if (speechRecognitionEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = speechStatusText, color = Color.White)
            }
        }

        if (settingsVisible) {
            SettingsDialog(
                gridEnabled = gridEnabled,
                timerSeconds = timerSeconds,
                onGridChange = {
                    gridEnabled = it
                    settingsManager.saveCameraSettings(
                        lensFacing, flashEnabled, gridEnabled, timerSeconds, speechRecognitionEnabled
                    )
                },
                onTimerChange = {
                    timerSeconds = it
                    settingsManager.saveCameraSettings(
                        lensFacing, flashEnabled, gridEnabled, timerSeconds, speechRecognitionEnabled
                    )
                },
                onDismissRequest = { settingsVisible = false },
                listeningEnabled = speechRecognitionEnabled,
                onListeningChange = { enabled ->
                    speechRecognitionEnabled = enabled
                    settingsManager.saveCameraSettings(
                        lensFacing, flashEnabled, gridEnabled, timerSeconds, enabled
                    )
                    if (enabled && speechModelInitialized && speechHelper.isModelReady()) {
                        speechHelper.startListening(
                            onCheeseHeard = { if (!timerActive) takePicture() },
                            onTimerHeard = { if (!timerActive) startTimerPhoto() }
                        )
                        speechStatusKey = SpeechStatusKey.LISTENING
                    } else {
                        speechHelper.stopListening()
                        speechStatusKey = SpeechStatusKey.PAUSED
                    }
                }
            )
        }
    }
}
