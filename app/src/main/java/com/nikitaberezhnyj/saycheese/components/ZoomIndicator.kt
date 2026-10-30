@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nikitaberezhnyj.saycheese.R
import com.nikitaberezhnyj.saycheese.theme.Accent
import com.nikitaberezhnyj.saycheese.theme.BackgroundDark
import com.nikitaberezhnyj.saycheese.theme.Spacing
import kotlin.math.abs

@Composable
fun ZoomIndicator(
    zoomRatio: Float,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = abs(zoomRatio - 1f) > 0.05f,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.zoom_format, zoomRatio),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Accent,
            modifier = Modifier
                .clip(CircleShape)
                .background(BackgroundDark.copy(alpha = 0.7f))
                .clickable(onClick = onReset)
                .padding(horizontal = Spacing.M, vertical = Spacing.S)
        )
    }
}