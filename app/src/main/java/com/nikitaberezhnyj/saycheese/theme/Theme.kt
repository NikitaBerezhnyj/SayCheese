@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = BackgroundDark,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHigh = SurfaceDark,
    outline = TextSecondary,
    error = DangerText
)

@Composable
fun SayCheeseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}