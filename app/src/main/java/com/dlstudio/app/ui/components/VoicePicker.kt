package com.dlstudio.app.ui.components

import android.speech.tts.Voice
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

/** Выбор голоса с понятными человеческими именами («Алёна», «Игорь» и т.д.). */
@Composable
fun VoicePicker(voices: List<Voice>, selected: String?, ui: Locale, onPreview: (Voice, String) -> Unit, onSelect: (Voice) -> Unit, onClose: () -> Unit) {
    val s = LocalStrings.current; val colors = MaterialTheme.colorScheme
    val names = remember(voices, ui) { VoiceName.labels(voices, ui) }
    DlModal.Shell(s.speechVoice, onClose) {
        Column(Modifier.heightIn(max = 390.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (voices.isEmpty()) Text(s.voicePackagesEmpty, color = colors.onSurfaceVariant)
            voices.sortedWith(compareBy({ it.locale.displayName }, { it.name })).forEach { voice ->
                val language = VoiceName.language(voice, ui)
                val on = voice.name == selected
                Surface(
                    Modifier.fillMaxWidth().clickable { onSelect(voice) }, RoundedCornerShape(16.dp),
                    if (on) colors.primaryContainer else colors.surfaceVariant,
                    border = BorderStroke(1.dp, if (on) colors.primary else colors.outlineVariant)
                ) {
                    Row(Modifier.padding(14.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(names[voice.name] ?: VoiceName.fallback(voice.name, ui), style = MaterialTheme.typography.titleSmall)
                            Text(language, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                        DlModal.Action("▶", { onPreview(voice, if (voice.locale.language == "ru") s.voicePreview else "Hello! This is how I sound.") })
                    }
                }
            }
        }
    }
}
