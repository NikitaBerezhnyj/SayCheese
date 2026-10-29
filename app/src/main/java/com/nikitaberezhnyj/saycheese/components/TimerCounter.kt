@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikitaberezhnyj.saycheese.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TimerCounter(
    totalSeconds: Int,
    onTimerFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var secondsLeft by remember(totalSeconds) { mutableStateOf(totalSeconds) }

    LaunchedEffect(totalSeconds) {
        secondsLeft = totalSeconds
        while (secondsLeft > 0) {
            delay(1000.milliseconds)
            secondsLeft -= 1
        }
        onTimerFinish()
    }

    Box(
        modifier = modifier
            .size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = secondsLeft.toString(),
            color = TextPrimary,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold
        )
    }
}