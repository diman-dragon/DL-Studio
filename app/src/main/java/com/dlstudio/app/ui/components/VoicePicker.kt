package com.dlstudio.app.ui.components

import android.speech.tts.Voice
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

/** Выбор голоса: список офлайн-голосов системы; «▶» проигрывает пример. Пока движок запускается, список пуст. */
@Composable
fun VoicePicker(voices: List<Voice>, selected: String?, onPreview: (Voice, String) -> Unit, onSelect: (Voice) -> Unit, onClose: () -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    DlModal.Shell(s.speechVoice, onClose) {
        Column(Modifier.heightIn(max = 390.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (voices.isEmpty()) Text(s.voicePackagesEmpty, color = colors.onSurfaceVariant)
            voices.sortedWith(compareBy({ it.locale.displayName }, { it.name })).forEach { voice ->
                val language = voice.locale.getDisplayName(voice.locale).replaceFirstChar { it.titlecase(voice.locale) }
                val on = voice.name == selected
                Surface(Modifier.fillMaxWidth().clickable { onSelect(voice) }, RoundedCornerShape(16.dp), if (on) colors.primaryContainer else colors.surfaceVariant, border = BorderStroke(1.dp, if (on) colors.primary else colors.outlineVariant)) {
                    Row(Modifier.padding(14.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(language); Text(voice.name.replace('_', ' '), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
                        DlModal.Action("▶", { onPreview(voice, s.voicePreview) })
                    }
                }
            }
        }
    }
}
