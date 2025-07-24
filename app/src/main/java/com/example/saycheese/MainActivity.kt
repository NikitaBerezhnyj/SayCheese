package com.example.saycheese

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import com.example.saycheese.ui.screens.CameraScreen
import com.example.saycheese.ui.theme.SayCheeseTheme
import com.example.saycheese.utils.Constants
import com.example.saycheese.utils.PermissionUtils

class MainActivity : ComponentActivity() {

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
                CameraScreen()
            }
        }
    }
}