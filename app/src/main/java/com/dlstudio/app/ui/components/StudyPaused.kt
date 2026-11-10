package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

/** Вкладка «Учёба» на время тестов новых карточек: неактивна, подсказывает, где сейчас открываются карточки. */
@Composable
fun StudyPaused() {
    val s = LocalStrings.current
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenHeader(s.tabStudy)
        Column(Modifier.weight(1f).padding(horizontal = 24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Text(s.stubHint, style = MaterialTheme.typography.titleLarge, color = muted, textAlign = TextAlign.Center)
            Text(s.studyPausedHint, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyLarge, color = muted, textAlign = TextAlign.Center)
        }
    }
}
