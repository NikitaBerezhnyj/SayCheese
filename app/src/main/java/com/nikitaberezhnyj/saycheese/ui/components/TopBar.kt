package com.nikitaberezhnyj.saycheese.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nikitaberezhnyj.saycheese.ui.theme.BackgroundDark
import com.nikitaberezhnyj.saycheese.ui.theme.TextPrimary

@Composable
fun TopBar(
    flashEnabled: Boolean,
    onFlashToggle: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(BackgroundDark)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onFlashToggle,
            modifier = Modifier.size(72.dp)
        ) {
            Icon(
                imageVector = if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                contentDescription = "Flash",
                tint = TextPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = TextPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}