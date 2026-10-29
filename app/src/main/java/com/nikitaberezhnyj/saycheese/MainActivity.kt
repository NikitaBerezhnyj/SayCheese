package com.nikitaberezhnyj.saycheese

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nikitaberezhnyj.saycheese.screens.CameraScreen
import com.nikitaberezhnyj.saycheese.screens.PermissionScreen
import com.nikitaberezhnyj.saycheese.theme.SayCheeseTheme
import com.nikitaberezhnyj.saycheese.models.SpeechStatusKey
import com.nikitaberezhnyj.saycheese.screens.ErrorScreen
import com.nikitaberezhnyj.saycheese.screens.LoadingScreen
import com.nikitaberezhnyj.saycheese.screens.SettingsScreen
import com.nikitaberezhnyj.saycheese.utils.LocaleUtils
import com.nikitaberezhnyj.saycheese.utils.PermissionUtils
import com.nikitaberezhnyj.saycheese.viewmodels.CameraViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val supportedLanguages = listOf("en", "uk")

        val savedLang = prefs.getString("language", null)
        val systemLang = Locale.getDefault().language

        val lang = when {
            savedLang != null && supportedLanguages.contains(savedLang) -> savedLang
            supportedLanguages.contains(systemLang) -> systemLang
            else -> "en"
        }

        super.attachBaseContext(
            LocaleUtils.setLocale(newBase, LocaleUtils.resolveLanguage(newBase))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            SayCheeseTheme {
                val viewModel: CameraViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                var permissionsGranted by remember {
                    mutableStateOf(PermissionUtils.allPermissionsGranted(this))
                }
                val refreshPermissions = {
                    permissionsGranted = PermissionUtils.allPermissionsGranted(this)
                }

                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { refreshPermissions() }

                LaunchedEffect(permissionsGranted, uiState.speechRecognitionEnabled) {
                    viewModel.initializeSpeechModel(permissionsGranted)
                }

                val speechNotReady =
                    uiState.speechRecognitionEnabled && !uiState.speechModelInitialized

                when {
                    !permissionsGranted -> PermissionScreen(onResult = refreshPermissions)

                    speechNotReady && uiState.speechStatusKey == SpeechStatusKey.ERROR_INIT ->
                        ErrorScreen(
                            onRetry = { viewModel.initializeSpeechModel(true) },
                            onContinueWithoutVoice = { viewModel.setSpeechRecognitionEnabled(false) }
                        )

                    speechNotReady -> LoadingScreen()

                    uiState.settingsVisible -> SettingsScreen()

                    else -> CameraScreen()
                }
            }
        }
    }
}