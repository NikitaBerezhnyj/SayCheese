@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
import com.nikitaberezhnyj.saycheese.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun SpeechHint(
    statusKey: SpeechStatusKey,
    heardCommand: VoiceCommand?,
    modifier: Modifier = Modifier
) {
    if (statusKey != SpeechStatusKey.LISTENING && statusKey != SpeechStatusKey.ERROR_INIT) return

    val isError = statusKey == SpeechStatusKey.ERROR_INIT
    val isHeard = heardCommand != null

    var expanded by remember { mutableStateOf(true) }
    LaunchedEffect(expanded) {
        if (expanded) {
            delay(4.seconds)
            expanded = false
        }
    }

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
        targetValue = if (isHeard) Accent else BackgroundDark.copy(alpha = 0.7f),
        label = "background"
    )

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(horizontal = Spacing.M, vertical = Spacing.S),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when {
                isHeard -> Icons.Outlined.Check
                isError -> Icons.Outlined.MicOff
                else -> Icons.Outlined.GraphicEq
            },
            contentDescription = null,
            tint = when {
                isHeard -> OnAccent
                isError -> DangerText
                else -> Accent
            },
            modifier = Modifier
                .size(IconSize.Medium)
                .scale(if (isHeard || isError) 1f else pulse)
        )

        if (expanded || isHeard) {
            Spacer(Modifier.width(Spacing.S))

            when {
                heardCommand != null -> Text(
                    text = stringResource(heardCommand.labelRes),
                    fontSize = FontSize.Body,
                    fontWeight = FontWeight.SemiBold,
                    color = OnAccent
                )

                isError -> Text(
                    text = stringResource(R.string.speech_unavailable),
                    fontSize = FontSize.Small,
                    color = TextPrimary
                )

                else -> Column {
                    Text(
                        text = stringResource(R.string.speech_hint_title),
                        fontSize = FontSize.Body,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(R.string.speech_hint_subtitle),
                        fontSize = FontSize.Small,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}