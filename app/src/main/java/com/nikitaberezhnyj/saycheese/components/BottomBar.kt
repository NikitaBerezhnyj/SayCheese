@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.nikitaberezhnyj.saycheese.R
import com.nikitaberezhnyj.saycheese.theme.BackgroundDark
import com.nikitaberezhnyj.saycheese.theme.BarHeight
import com.nikitaberezhnyj.saycheese.theme.IconSize
import com.nikitaberezhnyj.saycheese.theme.Spacing
import com.nikitaberezhnyj.saycheese.theme.TextPrimary

@Composable
fun BottomBar(
    lastPhotoUri: Uri?,
    iconRotation: () -> Float,
    onTakePhoto: () -> Unit,
    onSwitchCamera: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BarHeight.Bottom)
            .background(BackgroundDark)
            .padding(
                top = Spacing.S,
                bottom = Spacing.XXL,
                start = Spacing.M,
                end = Spacing.M,
            ),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PhotoThumbnail(lastPhotoUri, iconRotation)

        ShutterButton(onClick = onTakePhoto)

        IconButton(onClick = onSwitchCamera) {
            Icon(
                imageVector = Icons.Filled.ChangeCircle,
                contentDescription = stringResource(R.string.desc_switch_camera),
                tint = TextPrimary,
                modifier = Modifier.size(IconSize.Large)
            )
        }
    }
}