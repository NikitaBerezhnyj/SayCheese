package com.example.saycheese.ui.components

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.saycheese.R
import com.example.saycheese.utils.LocaleUtils

@Composable
fun SettingsDialog(
    gridEnabled: Boolean,
    timerSeconds: Int,
    listeningEnabled: Boolean,
    onGridChange: (Boolean) -> Unit,
    onTimerChange: (Int) -> Unit,
    onListeningChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val initialLanguage = prefs.getString("language", "uk") ?: "uk"

    var selectedLanguage by remember { mutableStateOf(initialLanguage) }
    var timerText by remember { mutableStateOf(timerSeconds.toString()) }
    var isError by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.settings_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = gridEnabled, onCheckedChange = onGridChange)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_show_grid))
                }

                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = listeningEnabled, onCheckedChange = onListeningChange)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_listening))
                }

                Spacer(Modifier.height(16.dp))

                Text(stringResource(R.string.settings_language))
                Spacer(Modifier.height(8.dp))
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            when (selectedLanguage) {
                                "uk" -> stringResource(R.string.settings_language_ukrainian)
                                "en" -> stringResource(R.string.settings_language_english)
                                else -> selectedLanguage
                            }
                        )
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_language_ukrainian)) },
                            onClick = {
                                selectedLanguage = "uk"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_language_english)) },
                            onClick = {
                                selectedLanguage = "en"
                                expanded = false
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(stringResource(R.string.settings_timer_label))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = timerText,
                    onValueChange = {
                        timerText = it
                        isError = it.toIntOrNull()?.let { v -> v < 0 } ?: true
                        if (!isError) onTimerChange(it.toInt())
                    },
                    isError = isError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        stringResource(R.string.settings_timer_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!isError) {
                        saveLanguagePreference(prefs, selectedLanguage)

                        if (selectedLanguage != initialLanguage) {
                            val activity = context as? ComponentActivity
                            activity?.recreate()
                        } else {
                            onDismissRequest()
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.settings_close))
            }
        }
    )
}

private fun saveLanguagePreference(prefs: SharedPreferences, lang: String) {
    prefs.edit().putString("language", lang).apply()
}
