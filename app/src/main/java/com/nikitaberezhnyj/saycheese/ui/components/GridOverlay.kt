package com.nikitaberezhnyj.saycheese.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun GridOverlay() {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val thirdWidth = width / 3
            val thirdHeight = height / 3

            for (i in 1..2) {
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = androidx.compose.ui.geometry.Offset(x = thirdWidth * i, y = 0f),
                    end = androidx.compose.ui.geometry.Offset(x = thirdWidth * i, y = height),
                    strokeWidth = 2f
                )
            }

            for (i in 1..2) {
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = androidx.compose.ui.geometry.Offset(x = 0f, y = thirdHeight * i),
                    end = androidx.compose.ui.geometry.Offset(x = width, y = thirdHeight * i),
                    strokeWidth = 2f
                )
            }
        }
    }
}