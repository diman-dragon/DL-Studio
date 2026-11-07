package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.study.SessionPraise
import com.dlstudio.app.study.StudySession
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.LocalStudyOptions
import com.dlstudio.app.ui.theme.gradientColors

/** Итог сессии с похвалой по результату (при «отлично» — конфетти); в режиме «Учить» сообщает о новом круге в другом порядке. */
@Composable
fun SessionDone(session: StudySession, onBack: () -> Unit) {
    val s = LocalStrings.current
    val level = SessionPraise.level(session.know, session.total)
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Box(Modifier.size(96.dp).background(Brush.linearGradient(LocalAccent.current.gradientColors()), CircleShape), contentAlignment = Alignment.Center) {
                AppIcon(R.drawable.ic_check, Color.White, 52.dp)
            }
            Text(s.sessionDone, Modifier.padding(top = 20.dp), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(s.sessionPraise(level), Modifier.padding(top = 6.dp), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
            Text(
                s.sessionSummary(session.know, session.hard, session.again), Modifier.padding(top = 6.dp, bottom = 12.dp),
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
            )
            if (session.random) Text(s.newRound, Modifier.padding(bottom = 20.dp), color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
            GradientButton(s.backToList, onBack)
        }
        if (level == 2 && LocalStudyOptions.current.celebrate) Confetti(Modifier.fillMaxSize())
    }
}
