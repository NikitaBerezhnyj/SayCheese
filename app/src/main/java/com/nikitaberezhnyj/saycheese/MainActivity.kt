package com.nikitaberezhnyj.saycheese

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.nikitaberezhnyj.saycheese.ui.screens.CameraScreen
import com.nikitaberezhnyj.saycheese.ui.screens.PermissionScreen
import com.nikitaberezhnyj.saycheese.ui.screens.SplashScreenContent
import com.nikitaberezhnyj.saycheese.ui.theme.SayCheeseTheme
import com.nikitaberezhnyj.saycheese.utils.Constants
import com.nikitaberezhnyj.saycheese.utils.LocaleUtils
import com.nikitaberezhnyj.saycheese.utils.PermissionUtils
import kotlinx.coroutines.delay
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

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

        val context = LocaleUtils.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SayCheeseTheme {
                var showSplash by remember { mutableStateOf(true) }
                var permissionsGranted by remember { mutableStateOf(PermissionUtils.allPermissionsGranted(this)) }

                requestPermissionLauncher =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
                        permissionsGranted = PermissionUtils.allPermissionsGranted(this)
                    }

                LaunchedEffect(Unit) {
                    delay(1500)
                    showSplash = false
                }

                when {
                    showSplash -> SplashScreenContent()
                    permissionsGranted -> CameraScreen()
                    else -> PermissionScreen {
                        requestPermissionLauncher.launch(Constants.REQUIRED_PERMISSIONS)
                    }
                }
            }
        }
    }
}