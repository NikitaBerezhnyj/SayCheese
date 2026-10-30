@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.nikitaberezhnyj.saycheese.theme.Highlight
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private val RingSize = 72.dp

@Composable
fun FocusRing(
    position: Offset,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1.4f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(position) {
        scale.snapTo(1.4f)
        alpha.snapTo(1f)
        scale.animateTo(1f, tween(200))
        delay(600.milliseconds)
        alpha.animateTo(0f, tween(200))
        onFinished()
    }

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    (position.x - RingSize.toPx() / 2).roundToInt(),
                    (position.y - RingSize.toPx() / 2).roundToInt()
                )
            }
            .size(RingSize)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
            .border(2.dp, Highlight, CircleShape)
    )
}