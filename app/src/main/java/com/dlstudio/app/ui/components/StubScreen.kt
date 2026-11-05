package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Заглушка раздела: заголовок и пометка «в разработке». */
@Composable
fun StubScreen(title: String, hint: String) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenHeader(title)
        Text(hint, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
