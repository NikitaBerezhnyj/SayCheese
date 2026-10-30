@file:Suppress("FunctionName")

package com.nikitaberezhnyj.saycheese.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nikitaberezhnyj.saycheese.R
import com.nikitaberezhnyj.saycheese.components.SettingsSection
import com.nikitaberezhnyj.saycheese.components.SettingsSegmentedRow
import com.nikitaberezhnyj.saycheese.components.SettingsSwitchRow
import com.nikitaberezhnyj.saycheese.models.PhotoFormat
import com.nikitaberezhnyj.saycheese.theme.BackgroundDark
import com.nikitaberezhnyj.saycheese.theme.FontSize
import com.nikitaberezhnyj.saycheese.theme.IconSize
import com.nikitaberezhnyj.saycheese.theme.Spacing
import com.nikitaberezhnyj.saycheese.theme.TextPrimary
import com.nikitaberezhnyj.saycheese.utils.LocaleUtils
import com.nikitaberezhnyj.saycheese.viewmodels.CameraViewModel

@Composable
fun SettingsScreen(
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage = remember { LocaleUtils.resolveLanguage(context) }

    BackHandler { viewModel.setSettingsVisible(false) }

    val timerOptions = listOf(0, 3, 5, 10).map { seconds ->
        seconds to if (seconds == 0) {
            stringResource(R.string.settings_timer_off)
        } else {
            stringResource(R.string.settings_timer_seconds, seconds)
        }
    }
    val formatOptions = PhotoFormat.entries.map { it to stringResource(it.labelRes) }
    val languageOptions = listOf(
        "uk" to stringResource(R.string.settings_language_ukrainian),
        "en" to stringResource(R.string.settings_language_english)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.S, vertical = Spacing.XS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.setSettingsVisible(false) }) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.desc_back),
                    tint = TextPrimary,
                    modifier = Modifier.size(IconSize.Medium)
                )
            }
            Spacer(Modifier.width(Spacing.S))
            Text(
                text = stringResource(R.string.settings_title),
                fontSize = FontSize.Subtitle,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.M)
                .padding(bottom = Spacing.L),
            verticalArrangement = Arrangement.spacedBy(Spacing.L)
        ) {
            SettingsSection(title = stringResource(R.string.settings_section_camera)) {
                SettingsSwitchRow(
                    icon = Icons.Outlined.GridOn,
                    label = stringResource(R.string.settings_show_grid),
                    checked = uiState.gridEnabled,
                    onCheckedChange = viewModel::setGridEnabled
                )
                SettingsSwitchRow(
                    icon = Icons.Outlined.Mic,
                    label = stringResource(R.string.settings_listening),
                    checked = uiState.speechRecognitionEnabled,
                    onCheckedChange = viewModel::setSpeechRecognitionEnabled,
                )
                SettingsSegmentedRow(
                    icon = Icons.Outlined.Timer,
                    label = stringResource(R.string.settings_timer_label),
                    options = timerOptions,
                    selected = uiState.timerSeconds,
                    onSelect = viewModel::setTimerSeconds
                )
                SettingsSegmentedRow(
                    icon = Icons.Outlined.AspectRatio,
                    label = stringResource(R.string.settings_photo_format),
                    options = formatOptions,
                    selected = uiState.photoFormat,
                    onSelect = viewModel::setPhotoFormat
                )
            }

            SettingsSection(title = stringResource(R.string.settings_section_app)) {
                SettingsSegmentedRow(
                    icon = Icons.Outlined.Language,
                    label = stringResource(R.string.settings_language),
                    options = languageOptions,
                    selected = currentLanguage,
                    onSelect = { language ->
                        if (language != currentLanguage) {
                            LocaleUtils.saveLanguage(context, language)
                            (context as Activity).recreate()
                        }
                    }
                )
            }
        }
    }
}