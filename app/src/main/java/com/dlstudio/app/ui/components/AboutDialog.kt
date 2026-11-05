package com.dlstudio.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.dlstudio.app.BuildConfig
import com.dlstudio.app.ui.theme.LocalStrings

/** «О нас»: название, версия сборки, описание. */
@Composable
fun AboutDialog(onClose: () -> Unit) {
    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("${s.aboutTitle} ${BuildConfig.VERSION_NAME}") },
        text = { Text(s.aboutText) },
        confirmButton = { TextButton(onClick = onClose) { Text(s.close) } }
    )
}
