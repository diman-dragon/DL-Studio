package com.dlstudio.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dlstudio.app.progress.Lesson
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings

/** Высота шапки урока, зазор под ней и высота связки между уроками: по ним карта считает, куда прокрутить к теме. */
internal val HEADER_H = 92.dp
internal val TRAIL_GAP = 12.dp
internal val LINK_H = 28.dp

/** Отступ центра кольца шапки от левого края: на этой линии идёт связка между уроками. */
private val SPINE = 42.dp

/**
 * Шапка урока на карте: кольцо «пройдено тем из всех» (у урока, где остановились, пульсирует), название, сколько пройдено и — в уроке,
 * где остановились, — на какой теме. Тап сворачивает или раскрывает тропу тем.
 */
@Composable
fun LessonHeader(lesson: Lesson, number: Int, done: Int, active: Boolean, lastTitle: String?, expanded: Boolean, onToggle: () -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)
    val total = lesson.topics.size
    val pulse by rememberInfiniteTransition(label = "lessonHere").animateFloat(1f, if (active) 1.26f else 1f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "lessonHere")
    Surface(
        Modifier.fillMaxWidth().height(HEADER_H).clip(shape).clickable(role = Role.Button, onClick = onToggle), shape,
        if (active) colors.primaryContainer else colors.surfaceVariant,
        border = BorderStroke(1.dp, if (active) colors.primary.copy(alpha = 0.6f) else colors.outlineVariant)
    ) {
        Row(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(14.dp), Alignment.CenterVertically) {
            Box(Modifier.size(52.dp), Alignment.Center) {
                if (active) Box(Modifier.size(52.dp).graphicsLayer { scaleX = pulse; scaleY = pulse }.background(colors.primary.copy(alpha = 0.18f), CircleShape))
                Ring(done / total.toFloat(), 52.dp, 5.dp) {
                    Text(if (done == total) "✓" else "$number", style = MaterialTheme.typography.titleMedium, color = if (done == total || active) colors.primary else colors.onSurfaceVariant)
                }
            }
            Column(Modifier.weight(1f), Arrangement.spacedBy(2.dp)) {
                Text(lesson.group.ifBlank { s.noGroup }, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(s.groupProgress(done, total), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                if (lastTitle != null) Text(s.lastSeen(lastTitle), style = MaterialTheme.typography.bodySmall, color = colors.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Chevron(expanded, colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun Chevron(open: Boolean, tint: Color) {
    val turn by animateFloatAsState(if (open) 180f else 0f, tween(300), label = "chevron")
    Canvas(Modifier.size(24.dp).graphicsLayer { rotationZ = turn }) {
        val path = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.38f)
            lineTo(size.width * 0.5f, size.height * 0.62f)
            lineTo(size.width * 0.75f, size.height * 0.38f)
        }
        drawPath(path, tint, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Связка между уроками: пройденный урок — сплошная линия цвета акцента, иначе пунктир. Она соединяет уроки и свёрнутыми, и раскрытыми. */
@Composable
fun LessonLink(walked: Boolean) {
    val accent = Color(LocalAccent.current.main)
    val track = MaterialTheme.colorScheme.outline
    Canvas(Modifier.fillMaxWidth().height(LINK_H)) {
        val x = SPINE.toPx()
        drawLine(
            if (walked) accent else track, Offset(x, 2.dp.toPx()), Offset(x, size.height - 2.dp.toPx()), (if (walked) 8.dp else 6.dp).toPx(), StrokeCap.Round,
            if (walked) null else PathEffect.dashPathEffect(floatArrayOf(0.1f, 20.dp.toPx()))
        )
    }
}
