package com.example.saycheese

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.example.saycheese.ui.screens.CameraScreen
import com.example.saycheese.ui.screens.PermissionScreen
import com.example.saycheese.ui.screens.SplashScreenContent
import com.example.saycheese.ui.theme.SayCheeseTheme
import com.example.saycheese.utils.Constants
import com.example.saycheese.utils.LocaleUtils
import com.example.saycheese.utils.PermissionUtils
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "en") ?: "en"
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

                // Реєстрація launcher тут, але оновлюємо стан замість recreate
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