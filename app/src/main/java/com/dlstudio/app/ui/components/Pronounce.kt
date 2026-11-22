package com.dlstudio.app.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dlstudio.app.R
import com.dlstudio.app.speech.Check
import com.dlstudio.app.speech.Grade
import com.dlstudio.app.speech.Phase
import com.dlstudio.app.speech.Pronounce
import com.dlstudio.app.speech.Pronunciation
import com.dlstudio.app.speech.Speak
import com.dlstudio.app.ui.theme.LocalStrings

private val MIC_COLORS = listOf(Color(0xFFFF9A3D), Color(0xFFFF3D77))
private val MIC_LIVE_COLORS = listOf(Color(0xFFFF5252), Color(0xFFC62828))

/** Фраза с озвучкой и проверкой произношения: динамик, микрофон и под ними результат последней проверки этой фразы. */
@Composable
fun PronounceRow(text: String, speak: Speak, check: Check?, modifier: Modifier = Modifier) {
    val mine = check?.state?.takeIf { it.text == text } ?: Pronounce()
    Column(modifier, Arrangement.spacedBy(8.dp), Alignment.CenterHorizontally) {
        Row(Modifier, Arrangement.spacedBy(16.dp), Alignment.CenterVertically) {
            SmallSpeaker(text, speak)
            if (check != null) MicButton(text, mine.phase == Phase.LISTENING, check)
        }
        PronounceResult(mine)
    }
}

/** Микрофон: тап — слушаем, повторный тап — отмена. Доступ к микрофону запрашивается при первом нажатии. */
@Composable
private fun MicButton(text: String, listening: Boolean, check: Check) {
    val context = LocalContext.current
    val s = LocalStrings.current
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) check.start(text) else check.denied(text) }
    val onClick: () -> Unit = {
        when {
            listening -> check.cancel()
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> check.start(text)
            else -> ask.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    GlowButton(R.drawable.ic_mic, if (listening) MIC_LIVE_COLORS else MIC_COLORS, onClick, s.checkPronunciation, pulse = listening)
}

@Composable
private fun PronounceResult(p: Pronounce) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val (line, color) = when (p.phase) {
        Phase.IDLE -> return
        Phase.LISTENING -> s.pronListening to colors.primary
        Phase.NO_SPEECH -> s.pronNoSpeech to colors.onSurfaceVariant
        Phase.NO_PERMISSION -> s.pronNoPermission to colors.error
        Phase.FAILED -> s.pronFailed to colors.error
        Phase.DONE -> when (Pronunciation.grade(p.score)) {
            Grade.GREAT -> s.pronGreat(p.score) to Color(0xFF34D399)
            Grade.GOOD -> s.pronGood(p.score) to Color(0xFFFFC857)
            Grade.RETRY -> s.pronRetry(p.score) to colors.error
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(line, style = MaterialTheme.typography.titleMedium, color = color, textAlign = TextAlign.Center)
        if (p.phase == Phase.DONE) Text(s.pronHeard(p.heard), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
