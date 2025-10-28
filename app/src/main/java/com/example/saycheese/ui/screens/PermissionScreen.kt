package com.example.saycheese.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.saycheese.R
import com.example.saycheese.ui.components.PermissionItem
import com.example.saycheese.utils.Constants
import com.example.saycheese.utils.PermissionUtils

@Composable
fun PermissionScreen(onPermissionsGranted: () -> Unit) {
    val context = LocalContext.current
    val activity = context as Activity
    val permissionMessage = stringResource(R.string.permission_required_self)

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (PermissionUtils.allPermissionsGranted(context)) {
            onPermissionsGranted()
        } else {
            Toast.makeText(context, permissionMessage, Toast.LENGTH_SHORT).show()
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.permission_title),
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.permission_description),
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            PermissionItem(
                icon = Icons.Filled.Camera,
                title = stringResource(R.string.permission_camera_title),
                description = stringResource(R.string.permission_camera_desc)
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionItem(
                icon = Icons.Filled.Mic,
                title = stringResource(R.string.permission_mic_title),
                description = stringResource(R.string.permission_mic_desc)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val deniedPermissions = Constants.REQUIRED_PERMISSIONS.filter {
                        !PermissionUtils.hasPermission(context, it)
                    }

                    if (deniedPermissions.isNotEmpty()) {
                        permissionLauncher.launch(deniedPermissions.toTypedArray())
                    } else {
                        Toast.makeText(context, permissionMessage, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .scale(buttonScale),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = stringResource(R.string.grant_permissions),
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
    }
}
