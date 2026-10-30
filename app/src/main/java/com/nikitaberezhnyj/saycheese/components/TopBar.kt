@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import com.nikitaberezhnyj.saycheese.R
import com.nikitaberezhnyj.saycheese.theme.BarHeight
import com.nikitaberezhnyj.saycheese.theme.IconSize
import com.nikitaberezhnyj.saycheese.theme.Spacing
import com.nikitaberezhnyj.saycheese.theme.TextPrimary

@Composable
fun TopBar(
    flashEnabled: Boolean,
    iconRotation: () -> Float,
    onFlashToggle: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BarHeight.Top)
            .padding(horizontal = Spacing.S),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onFlashToggle) {
            Icon(
                imageVector = if (flashEnabled) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                contentDescription = stringResource(R.string.desc_flash),
                tint = TextPrimary,
                modifier = Modifier
                    .size(IconSize.Medium)
                    .graphicsLayer { rotationZ = iconRotation() }
            )
        }

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.desc_settings),
                tint = TextPrimary,
                modifier = Modifier
                    .size(IconSize.Medium)
                    .graphicsLayer { rotationZ = iconRotation() }
            )
        }
    }
}