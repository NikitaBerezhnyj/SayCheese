package com.nikitaberezhnyj.saycheese.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.*

@Composable
fun rememberIconRotation(deviceRotation: Int): State<Float> {
    var target by remember { mutableFloatStateOf(deviceRotation * 90f) }

    LaunchedEffect(deviceRotation) {
        val delta = ((deviceRotation * 90f - target) % 360f + 540f) % 360f - 180f
        target += delta
    }

    return animateFloatAsState(targetValue = target, label = "iconRotation")
}