@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.nikitaberezhnyj.saycheese.R
import com.nikitaberezhnyj.saycheese.components.AppLogo
import com.nikitaberezhnyj.saycheese.components.PermissionItem
import com.nikitaberezhnyj.saycheese.components.PrimaryButton
import com.nikitaberezhnyj.saycheese.components.ScreenLayout
import com.nikitaberezhnyj.saycheese.constants.Constants
import com.nikitaberezhnyj.saycheese.theme.DangerText
import com.nikitaberezhnyj.saycheese.theme.Spacing
import com.nikitaberezhnyj.saycheese.utils.PermissionUtils

@Composable
fun PermissionScreen(onResult: () -> Unit) {
    val context = LocalContext.current
    val activity = context as Activity

    var blocked by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        blocked = Constants.REQUIRED_PERMISSIONS.any {
            !PermissionUtils.hasPermission(context, it) &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
        onResult()
    }

    ScreenLayout(
        icon = { AppLogo() },
        title = stringResource(R.string.permission_title),
        description = stringResource(R.string.permission_description)
    ) {
        PermissionItem(
            icon = Icons.Filled.Camera,
            title = stringResource(R.string.permission_camera_title),
            description = stringResource(R.string.permission_camera_desc)
        )

        Spacer(Modifier.height(Spacing.S))

        PermissionItem(
            icon = Icons.Filled.Mic,
            title = stringResource(R.string.permission_mic_title),
            description = stringResource(R.string.permission_mic_desc)
        )

        Spacer(Modifier.height(Spacing.L))

        if (blocked) {
            Text(
                text = stringResource(R.string.permission_blocked),
                fontSize = 14.sp,
                color = DangerText,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.M))
        }

        PrimaryButton(
            text = stringResource(
                if (blocked) R.string.permission_open_settings else R.string.grant_permissions
            ),
            onClick = {
                if (blocked) {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                    )
                } else {
                    launcher.launch(Constants.REQUIRED_PERMISSIONS)
                }
            }
        )
    }
}