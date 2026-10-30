@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.components

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
import com.nikitaberezhnyj.saycheese.theme.FontSize
import com.nikitaberezhnyj.saycheese.theme.Highlight
import com.nikitaberezhnyj.saycheese.theme.Spacing
import com.nikitaberezhnyj.saycheese.theme.TextPrimary
import kotlin.math.abs

@Composable
fun ZoomIndicator(
    zoomRatio: Float,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isZoomed = zoomRatio > 1.05f

    Text(
        text = stringResource(R.string.zoom_format, zoomRatio),
        fontSize = FontSize.Small,
        fontWeight = FontWeight.SemiBold,
        color = if (isZoomed) Highlight else TextPrimary,
        modifier = modifier
            .clip(CircleShape)
            .background(BackgroundDark.copy(alpha = 0.55f))
            .clickable(onClick = onReset)
            .padding(horizontal = Spacing.M, vertical = Spacing.S)
    )
}