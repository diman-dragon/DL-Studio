package com.dlstudio.app.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.R
import com.dlstudio.app.ui.components.AppIcon
import com.dlstudio.app.ui.components.FitText

/** Заголовок: название открытого узла; внутри проекта или колоды слева стрелка «назад». */
@Composable
fun LibraryHeader(title: String, canGoUp: Boolean, onUp: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        if (canGoUp) {
            IconButton(onClick = onUp) { AppIcon(R.drawable.ic_back, MaterialTheme.colorScheme.onBackground, 30.dp) }
        }
        FitText(title, MaterialTheme.typography.headlineMedium, Modifier.weight(1f), minSize = 18.sp)
    }
}
