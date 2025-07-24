package com.example.saycheese.ui.components

import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun BottomBar(
    imageCapture: MutableState<ImageCapture?>,
    onTakePhoto: () -> Unit,
    onTakePhotoWithTimer: () -> Unit,
    onSwitchCamera: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(124.dp)
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                Log.d("CameraX", "Timer button clicked - starting timer")
                val currentImageCapture = imageCapture.value
                if (currentImageCapture == null) {
                    Log.e("CameraX", "ImageCapture is null when trying to start timer")
                    Toast.makeText(context, "Камера не готова. Спробуйте ще раз.", Toast.LENGTH_SHORT).show()
                    return@IconButton
                }
                onTakePhotoWithTimer()
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = "Take Photo with Timer",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        IconButton(
            onClick = {
                Log.d("CameraX", "Take photo clicked (BottomBar)")
                val currentImageCapture = imageCapture.value
                if (currentImageCapture == null) {
                    Log.e("CameraX", "ImageCapture is null when trying to take photo")
                    Toast.makeText(context, "Камера не готова. Спробуйте ще раз.", Toast.LENGTH_SHORT).show()
                    return@IconButton
                }
                onTakePhoto()
            },
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(
                imageVector = Icons.Filled.CameraEnhance,
                contentDescription = "Take Photo",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }

        IconButton(
            onClick = onSwitchCamera,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ChangeCircle,
                contentDescription = "Switch Camera",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}