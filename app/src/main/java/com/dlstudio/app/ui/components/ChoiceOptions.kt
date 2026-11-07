package com.dlstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 4 варианта ответа в виде сетки 2x2 под карточкой с индикацией правильности. */
@Composable
fun ChoiceOptions(
    options: List<String>,
    correct: String,
    selected: String?,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val rows = options.chunked(2)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { opt ->
                    val isChosen = selected != null && opt == selected
                    val isTarget = selected != null && opt == correct
                    val bg = when {
                        isChosen && opt == correct -> Color(0xFF2E7D32).copy(alpha = 0.22f)
                        isChosen -> Color(0xFFC62828).copy(alpha = 0.22f)
                        isTarget -> Color(0xFF2E7D32).copy(alpha = 0.16f)
                        else -> cs.surfaceContainerHigh
                    }
                    val border = when {
                        isChosen && opt == correct -> BorderStroke(1.5.dp, Color(0xFF2E7D32))
                        isChosen -> BorderStroke(1.5.dp, Color(0xFFC62828))
                        isTarget -> BorderStroke(1.5.dp, Color(0xFF2E7D32).copy(alpha = 0.7f))
                        else -> BorderStroke(1.dp, cs.outlineVariant)
                    }
                    Surface(
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = bg,
                        border = border,
                        onClick = { if (selected == null) onPick(opt) }
                    ) {
                        Box(Modifier.fillMaxSize().padding(horizontal = 10.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            FitText(opt, MaterialTheme.typography.titleSmall.copy(textAlign = TextAlign.Center), color = cs.onSurface, minSize = 11.sp, maxLines = 2)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
