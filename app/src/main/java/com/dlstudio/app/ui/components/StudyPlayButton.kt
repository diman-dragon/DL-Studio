package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.gradientColors

/** Кнопка «Учить» в цвете акцента: запускает случайный круг по всем карточкам проекта или колоды. */
@Composable
fun StudyPlayButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = LocalStrings.current.learn
    Box(
        modifier.size(48.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(LocalAccent.current.gradientColors()))
            .clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) { AppIcon(R.drawable.ic_play, Color.White, 22.dp) }
}
