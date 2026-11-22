package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.content.Topic
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.gradientColors

/** Большая карточка «Продолжить»: следующая тема в цвете акцента, три точки — этапы (теория, карточки, практика). */
@Composable
fun ContinueCard(topic: Topic, stages: Int, onClick: () -> Unit) {
    val s = LocalStrings.current
    val accent = LocalAccent.current
    val shape = RoundedCornerShape(28.dp)
    Row(
        Modifier.fillMaxWidth().shadow(16.dp, shape, ambientColor = Color(accent.main), spotColor = Color(accent.main)).clip(shape)
            .background(Brush.linearGradient(accent.gradientColors())).clickable(role = Role.Button, onClick = onClick).padding(20.dp),
        Arrangement.spacedBy(16.dp), Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), Arrangement.spacedBy(6.dp)) {
            Text(s.continueLabel, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
            Text(topic.title, color = Color.White, style = MaterialTheme.typography.titleLarge, maxLines = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { Box(Modifier.size(8.dp).background(Color.White.copy(alpha = if (it < stages) 1f else 0.35f), CircleShape)) }
            }
        }
        Box(Modifier.size(56.dp).background(Color.White.copy(alpha = 0.22f), CircleShape), Alignment.Center) { AppIcon(R.drawable.ic_play, Color.White, 32.dp) }
    }
}
