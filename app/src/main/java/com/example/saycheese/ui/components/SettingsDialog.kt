package com.example.saycheese.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions

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
    var timerText by remember { mutableStateOf(timerSeconds.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Налаштування") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = gridEnabled,
                        onCheckedChange = onGridChange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Показувати сітку")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = listeningEnabled,
                        onCheckedChange = onListeningChange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Слухання команд")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Таймер (секунди):", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = timerText,
                    onValueChange = {
                        timerText = it
                        isError = it.toIntOrNull()?.let { value -> value < 0 } ?: true
                        if (!isError) {
                            onTimerChange(it.toInt())
                        }
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
                        text = "Введіть коректне число (0 або більше)",
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
                    if (!isError) onDismissRequest()
                }
            ) {
                Text("Закрити")
            }
        }
    )
}

