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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.saycheese.data.CameraSettingsManager
import com.example.saycheese.ui.components.*
import com.example.saycheese.utils.PermissionUtils
import com.example.saycheese.utils.SpeechRecognizerHelper
import com.example.saycheese.utils.takePhoto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val settingsManager = remember { CameraSettingsManager(context) }

    val (savedLensFacing, savedFlashEnabled, savedGridEnabled, savedTimeSecond, savedSpeechRecognizationEnabled) =
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
    var speechRecognizationEnabled by remember { mutableStateOf(savedSpeechRecognizationEnabled) }

    val hasPermissions = remember {
        PermissionUtils.hasCameraPermission(context) && PermissionUtils.hasAudioPermission(context)
    }

    val speechHelper = remember { SpeechRecognizerHelper(context) }
    var speechModelInitialized by remember { mutableStateOf(false) }
    var speechStatus by remember { mutableStateOf("Ініціалізація...") }

    LaunchedEffect(Unit) {
        if (hasPermissions) {
            speechStatus = "Завантаження моделі..."
            try {
                speechHelper.initModel()
                speechModelInitialized = true
                speechStatus = "Модель готова до роботи"
                Log.d("SpeechRecognition", "Vosk model loaded successfully")
            } catch (e: Exception) {
                Log.e("SpeechRecognition", "Model init failed", e)
                speechStatus = "Помилка ініціалізації моделі"
                speechModelInitialized = false
            }
        }
    }

    fun takePicture() {
        val capture = imageCapture.value
        if (capture == null) {
            Log.e("CameraX", "❌ ImageCapture is null - camera not ready yet")
            return
        }
        Log.d("CameraX", "✅ Taking picture with valid ImageCapture")
        takePhoto(context, capture) { flashVisible = true }
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
        coroutineScope.launch {
            delay(100)
            takePicture()
        }
    }

    DisposableEffect(lifecycleOwner, speechModelInitialized, speechRecognizationEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d("SpeechRecognition", "ON_RESUME event")
                    if (speechRecognizationEnabled && speechModelInitialized && speechHelper.isModelReady()) {
                        Log.d("SpeechRecognition", "✅ Starting listening...")
                        speechHelper.startListening(
                            onCheeseHeard = {
                                Log.d("SpeechRecognition", "🧀 Cheese command received!")
                                if (!timerActive) takePicture()
                            },
                            onTimerHeard = {
                                Log.d("SpeechRecognition", "⏱️ Timer command received!")
                                if (!timerActive) startTimerPhoto()
                            }
                        )
                        speechStatus = "🎤 Слухаю команду..."
                    } else {
                        val reason = when {
                            !speechRecognizationEnabled -> "вимкнено в налаштуваннях"
                            !speechModelInitialized -> "модель ще завантажується"
                            !speechHelper.isModelReady() -> "модель не готова"
                            else -> "невідома причина"
                        }
                        speechStatus = "Розпізнавання не активне: $reason"
                        Log.d("SpeechRecognition", "Not starting listener: $reason")
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d("SpeechRecognition", "ON_PAUSE event - stopping listener")
                    speechHelper.stopListening()
                    speechStatus = "⏸️ Призупинено"
                }
                Lifecycle.Event.ON_DESTROY -> {
                    Log.d("SpeechRecognition", "ON_DESTROY event - releasing resources")
                    speechHelper.release()
                    speechStatus = "⏹️ Розпізнавач зупинено"
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            speechHelper.release()
        }
    }

    if (!hasPermissions) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Потрібен дозвіл на камеру та мікрофон", color = Color.White)
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
                        lensFacing, flashEnabled, gridEnabled, timerSeconds, speechRecognizationEnabled
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
                if (timerActive)
                    TimerCounter(timerSeconds, ::onTimerFinish, Modifier.align(Alignment.Center))
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
                            lensFacing, flashEnabled, gridEnabled, timerSeconds, speechRecognizationEnabled
                        )
                    }
                }
            )
        }

        if (speechRecognizationEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = speechStatus, color = Color.White)
            }
        }

        if (settingsVisible) {
            SettingsDialog(
                gridEnabled = gridEnabled,
                timerSeconds = timerSeconds,
                onGridChange = {
                    gridEnabled = it
                    settingsManager.saveCameraSettings(
                        lensFacing, flashEnabled, gridEnabled, timerSeconds, speechRecognizationEnabled
                    )
                },
                onTimerChange = {
                    timerSeconds = it
                    settingsManager.saveCameraSettings(
                        lensFacing, flashEnabled, gridEnabled, timerSeconds, speechRecognizationEnabled
                    )
                },
                onDismissRequest = { settingsVisible = false },
                listeningEnabled = speechRecognizationEnabled,
                onListeningChange = { enabled ->
                    speechRecognizationEnabled = enabled
                    settingsManager.saveCameraSettings(
                        lensFacing, flashEnabled, gridEnabled, timerSeconds, enabled
                    )
                    if (enabled && speechModelInitialized && speechHelper.isModelReady()) {
                        speechHelper.startListening(
                            onCheeseHeard = { if (!timerActive) takePicture() },
                            onTimerHeard = { if (!timerActive) startTimerPhoto() }
                        )
                        speechStatus = "🎤 Слухаю команду..."
                    } else {
                        speechHelper.stopListening()
                        speechStatus = "⏸️ Призупинено"
                    }
                }
            )
        }
    }
}