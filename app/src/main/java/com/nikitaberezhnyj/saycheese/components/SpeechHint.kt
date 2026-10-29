@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nikitaberezhnyj.saycheese.R
import com.nikitaberezhnyj.saycheese.models.SpeechStatusKey
import com.nikitaberezhnyj.saycheese.theme.Accent
import com.nikitaberezhnyj.saycheese.theme.BackgroundDark
import com.nikitaberezhnyj.saycheese.theme.DangerText
import com.nikitaberezhnyj.saycheese.theme.IconSize
import com.nikitaberezhnyj.saycheese.theme.Radius
import com.nikitaberezhnyj.saycheese.theme.Spacing
import com.nikitaberezhnyj.saycheese.theme.TextPrimary
import com.nikitaberezhnyj.saycheese.theme.TextSecondary

@Composable
fun SpeechHint(
    statusKey: SpeechStatusKey,
    modifier: Modifier = Modifier
) {
    if (statusKey != SpeechStatusKey.LISTENING && statusKey != SpeechStatusKey.ERROR_INIT) return

    val isError = statusKey == SpeechStatusKey.ERROR_INIT

    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Row(
        modifier = modifier
            .background(BackgroundDark.copy(alpha = 0.7f), RoundedCornerShape(Radius.M))
            .padding(horizontal = Spacing.M, vertical = Spacing.S),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isError) Icons.Default.MicOff else Icons.Default.GraphicEq,
            contentDescription = null,
            tint = if (isError) DangerText else Accent,
            modifier = Modifier
                .size(IconSize.Medium)
                .scale(if (isError) 1f else pulse)
        )

        Spacer(Modifier.width(Spacing.S))

        if (isError) {
            Text(
                text = stringResource(R.string.speech_unavailable),
                fontSize = 14.sp,
                color = TextPrimary
            )
        } else {
            Column {
                Text(
                    text = stringResource(R.string.speech_hint_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = stringResource(R.string.speech_hint_subtitle),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}