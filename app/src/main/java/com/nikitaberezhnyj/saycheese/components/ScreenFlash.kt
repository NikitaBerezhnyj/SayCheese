@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.components

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.nikitaberezhnyj.saycheese.models.ScreenFlashMode

private const val VEIL_ALPHA = 0.5f

@Composable
fun ScreenFlash(mode: ScreenFlashMode) {
    val window = (LocalContext.current as Activity).window

    DisposableEffect(Unit) {
        val params = window.attributes
        val previousBrightness = params.screenBrightness
        params.screenBrightness = 1f
        window.attributes = params

        onDispose {
            val restored = window.attributes
            restored.screenBrightness = previousBrightness
            window.attributes = restored
        }
    }

    val alpha = remember { Animatable(0f) }

    LaunchedEffect(mode) {
        if (mode == ScreenFlashMode.FLASH) {
            alpha.animateTo(1f, tween(80))
        } else {
            alpha.animateTo(VEIL_ALPHA, tween(250))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha.value }
            .background(Color.White)
    )
}