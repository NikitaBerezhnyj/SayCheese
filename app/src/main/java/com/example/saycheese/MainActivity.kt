package com.example.saycheese

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import com.example.saycheese.ui.screens.CameraScreen
import com.example.saycheese.ui.screens.SplashScreenContent
import com.example.saycheese.ui.theme.SayCheeseTheme
import com.example.saycheese.utils.Constants
import com.example.saycheese.utils.LocaleUtils
import com.example.saycheese.utils.PermissionUtils
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "en") ?: "en"
        val context = LocaleUtils.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!PermissionUtils.allPermissionsGranted(this)) {
            ActivityCompat.requestPermissions(
                this,
                Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSIONS
            )
        }

        setContent {
            SayCheeseTheme {
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(1500)
                    showSplash = false
                }

                if (showSplash) {
                    SplashScreenContent()
                } else {
                    CameraScreen()
                }
            }
        }
    }
}
