package com.example.pocketstorage.components

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketstorage.R
import com.example.pocketstorage.utils.SnackbarManager
import com.example.pocketstorage.utils.SnackbarMessage
import com.example.pocketstorage.utils.SnackbarMessage.Companion.toMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SnackBarToast(
    snackbarMessage: SnackbarMessage?,
    context: Context
) {
    snackbarMessage?.let { message ->
        val coroutineScope = rememberCoroutineScope()
        val snackbarVisibleState = remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            if (snackbarVisibleState.value) {
                SnackbarManager.showMessage(message.toMessage(context.resources))
            }
        }

        DisposableEffect(key1 = message) {
            onDispose {
                snackbarVisibleState.value = false
            }
        }

        DisposableEffect(key1 = snackbarVisibleState.value) {
            if (snackbarVisibleState.value) {
                coroutineScope.launch {
                    delay(2000) // Задержка в 2 секунды
                    SnackbarManager.clearSnackbarState()
                    snackbarVisibleState.value = false
                }
            }

            onDispose {
                snackbarVisibleState.value = false
            }

        }

        Snackbar(
            modifier = Modifier.padding(8.dp),
            actionOnNewLine = true,
            dismissAction = {
                TextButton(onClick = {
                    SnackbarManager.clearSnackbarState()
                    snackbarVisibleState.value = false
                }) {
                    Text(text = "Закрыть", color = colorResource(id = R.color.AdamantineBlue))
                }
            }
        ) {
            Text(message.toMessage(context.resources), fontSize = 12.sp)
        }
    }
}