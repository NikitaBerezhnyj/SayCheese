@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.nikitaberezhnyj.saycheese.R
import com.nikitaberezhnyj.saycheese.components.PrimaryButton
import com.nikitaberezhnyj.saycheese.components.ScreenLayout
import com.nikitaberezhnyj.saycheese.theme.DangerText
import com.nikitaberezhnyj.saycheese.theme.IconSize
import com.nikitaberezhnyj.saycheese.theme.Spacing

@Composable
fun ErrorScreen(
    onRetry: () -> Unit,
    onContinueWithoutVoice: () -> Unit
) {
    ScreenLayout(
        icon = {
            Icon(
                imageVector = Icons.Default.ReportProblem,
                contentDescription = null,
                tint = DangerText,
                modifier = Modifier.size(IconSize.XLarge)
            )
        },
        title = stringResource(R.string.error_voice_title),
        description = stringResource(R.string.error_voice_message)
    ) {
        PrimaryButton(
            text = stringResource(R.string.error_retry),
            onClick = onRetry
        )

        Spacer(Modifier.height(Spacing.S))

        TextButton(onClick = onContinueWithoutVoice) {
            Text(stringResource(R.string.error_continue_without_voice))
        }
    }
}