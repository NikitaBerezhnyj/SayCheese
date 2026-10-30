@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nikitaberezhnyj.saycheese.R
import com.nikitaberezhnyj.saycheese.theme.IconSize
import com.nikitaberezhnyj.saycheese.theme.Radius
import com.nikitaberezhnyj.saycheese.theme.SurfaceDark
import com.nikitaberezhnyj.saycheese.theme.TextSecondary

@Composable
fun PhotoThumbnail(uri: Uri?, rotation: () -> Float) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer { rotationZ = rotation() }
            .clip(RoundedCornerShape(Radius.S))
            .background(SurfaceDark)
            .then(if (uri != null) Modifier.clickable { openPhoto(context, uri) } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = stringResource(R.string.desc_last_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(IconSize.Small)
            )
        }
    }
}

private fun openPhoto(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "image/jpeg")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(intent) }
}