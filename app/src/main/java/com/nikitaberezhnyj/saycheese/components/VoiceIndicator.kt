@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nikitaberezhnyj.saycheese.R
import com.nikitaberezhnyj.saycheese.models.SpeechStatusKey
import com.nikitaberezhnyj.saycheese.models.VoiceCommand
import com.nikitaberezhnyj.saycheese.theme.Accent
import com.nikitaberezhnyj.saycheese.theme.BackgroundDark
import com.nikitaberezhnyj.saycheese.theme.DangerText
import com.nikitaberezhnyj.saycheese.theme.FontSize
import com.nikitaberezhnyj.saycheese.theme.IconSize
import com.nikitaberezhnyj.saycheese.theme.OnAccent
import com.nikitaberezhnyj.saycheese.theme.Spacing
import com.nikitaberezhnyj.saycheese.theme.TextPrimary

@Composable
fun VoiceIndicator(
    statusKey: SpeechStatusKey,
    heardCommand: VoiceCommand?,
    modifier: Modifier = Modifier
) {
    if (statusKey != SpeechStatusKey.LISTENING && statusKey != SpeechStatusKey.ERROR_INIT) return

    val isError = statusKey == SpeechStatusKey.ERROR_INIT
    val isHeard = heardCommand != null

    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val background by animateColorAsState(
        targetValue = if (isHeard) Accent else BackgroundDark.copy(alpha = 0.55f),
        label = "background"
    )

    val description = when {
        isHeard -> null
        isError -> stringResource(R.string.speech_unavailable)
        else -> stringResource(R.string.desc_voice_listening)
    }

    Row(
        modifier = modifier
            .height(44.dp)
            .widthIn(min = 44.dp)
            .clip(CircleShape)
            .background(background)
            .animateContentSize()
            .padding(horizontal = if (isHeard) Spacing.M else 0.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when {
                isHeard -> Icons.Outlined.Check
                isError -> Icons.Outlined.MicOff
                else -> Icons.Outlined.Mic
            },
            contentDescription = description,
            tint = when {
                isHeard -> OnAccent
                isError -> DangerText
                else -> TextPrimary
            },
            modifier = Modifier
                .size(IconSize.Medium)
                .scale(if (isHeard || isError) 1f else pulse)
        )

        if (heardCommand != null) {
            Spacer(Modifier.width(Spacing.S))
            Text(
                text = stringResource(heardCommand.labelRes),
                fontSize = FontSize.Body,
                fontWeight = FontWeight.SemiBold,
                color = OnAccent
            )
        }
    }
}