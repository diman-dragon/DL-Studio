package com.dlstudio.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import com.dlstudio.app.BuildConfig
import com.dlstudio.app.ui.theme.LocalStrings

/** «О нас»: название, версия сборки, описание. */
@Composable
fun AboutDialog(onClose: () -> Unit) {
    val s = LocalStrings.current
    DlModal.Shell("${s.aboutTitle} ${BuildConfig.VERSION_NAME}", onClose) {
        Text(s.aboutText)
        androidx.compose.foundation.layout.Row(androidx.compose.ui.Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) { DlModal.Action(s.close, onClose) }
    }
}
