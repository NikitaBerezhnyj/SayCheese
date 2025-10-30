package com.nikitaberezhnyj.saycheese.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nikitaberezhnyj.saycheese.R
import com.nikitaberezhnyj.saycheese.ui.screens.SpeechStatusKey
import com.nikitaberezhnyj.saycheese.ui.theme.BackgroundDark
import com.nikitaberezhnyj.saycheese.ui.theme.DangerText
import com.nikitaberezhnyj.saycheese.ui.theme.InfoText
import com.nikitaberezhnyj.saycheese.ui.theme.TextPrimary

@Composable
fun SpeechStatusIndicator(
    statusKey: SpeechStatusKey,
    modifier: Modifier = Modifier
) {
    val (icon, contentDescription) = when (statusKey) {
        SpeechStatusKey.INIT ->
            Icons.Default.HourglassEmpty to stringResource(R.string.speech_init)
        SpeechStatusKey.LOADING ->
            Icons.Default.CloudDownload to stringResource(R.string.speech_loading_model)
        SpeechStatusKey.READY ->
            Icons.Default.TaskAlt to stringResource(R.string.speech_ready)
        SpeechStatusKey.ERROR_INIT ->
            Icons.Default.ReportProblem to stringResource(R.string.speech_error_init)
        SpeechStatusKey.LISTENING ->
            Icons.Default.GraphicEq to stringResource(R.string.speech_listening)
        SpeechStatusKey.PAUSED ->
            Icons.Default.PauseCircle to stringResource(R.string.speech_paused)
        SpeechStatusKey.STOPPED ->
            Icons.Default.StopCircle to stringResource(R.string.speech_stopped)
        SpeechStatusKey.INACTIVE ->
            Icons.Default.MicOff to stringResource(R.string.speech_inactive, "")
    }

    val iconColor = when (statusKey) {
        SpeechStatusKey.ERROR_INIT -> DangerText
        SpeechStatusKey.INACTIVE,
        SpeechStatusKey.PAUSED,
        SpeechStatusKey.STOPPED -> InfoText
        else -> TextPrimary
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val shouldAnimate = statusKey == SpeechStatusKey.LOADING || statusKey == SpeechStatusKey.LISTENING

    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                color = BackgroundDark.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier
                .size(32.dp)
                .then(if (shouldAnimate) Modifier.scale(scale) else Modifier)
        )
    }
}