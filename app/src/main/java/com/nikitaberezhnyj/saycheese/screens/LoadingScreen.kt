@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.screens

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nikitaberezhnyj.saycheese.R
import com.nikitaberezhnyj.saycheese.components.AppLogo
import com.nikitaberezhnyj.saycheese.components.ScreenLayout

@Composable
fun LoadingScreen() {
    ScreenLayout(
        icon = { AppLogo() },
        title = stringResource(R.string.app_name),
        description = stringResource(R.string.loading_voice)
    ) {
        CircularProgressIndicator()
    }
}