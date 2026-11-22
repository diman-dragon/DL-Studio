package com.dlstudio.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.content.Topic
import com.dlstudio.app.progress.TopicProgress
import com.dlstudio.app.progress.Xp
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.gradientColors

/** Высота одной строки тропы и графа; ширина подписи узла по умолчанию. */
internal val ROW = 176.dp
internal val LABEL = 148.dp

/** Подпись узла занимает ровно две строки, чтобы линия всегда начиналась под ней. */
private val LABEL_HEIGHT = 40.dp

/**
 * Тропа группы: тема за темой по извилистой линии. Пройденный участок залит цветом акцента, дальше — пунктир.
 * Первая непройденная тема — «вы здесь» (кольцо этапов и пульсирующее свечение). Открыть можно любую тему.
 */
@Composable
fun Trail(topics: List<Topic>, progress: Map<String, TopicProgress>, onOpen: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val accent = Color(LocalAccent.current.main)
    val stages = topics.map { Xp.stagesDone(it, progress[it.id] ?: TopicProgress()) }
    val here = stages.indexOfFirst { it < 3 }
    BoxWithConstraints(Modifier.fillMaxWidth().height(ROW * topics.size)) {
        val xs = topics.indices.map { maxWidth * (if (it % 2 == 0) 0.28f else 0.72f) }
        Canvas(Modifier.fillMaxSize()) {
            val row = ROW.toPx()
            for (i in 0 until topics.lastIndex) {
                val from = Offset(xs[i].toPx(), row * i + row / 2)
                val to = Offset(xs[i + 1].toPx(), row * (i + 1) + row / 2)
                drawEdge(from, to, stages[i] == 3, accent, colors.outline)
            }
        }
        topics.forEachIndexed { i, topic -> Stop(topic, i + 1, stages[i], i == here, false, xs[i], ROW * i + ROW / 2, LABEL, onOpen) }
    }
}

/** Где линия выходит из узла (ниже его подписи) и где входит в следующий (над кружком с запасом на свечение): расстояние от центра. */
private val EDGE_OUT = 90.dp
private val EDGE_IN = 56.dp

/** Линия от узла к узлу, центры — `from` и `to`: начинается под подписью и кончается перед кружком, не заходя на них. Пройденная — сплошная, остальная — пунктир. */
internal fun DrawScope.drawEdge(centerFrom: Offset, centerTo: Offset, walked: Boolean, accent: Color, track: Color) {
    val from = Offset(centerFrom.x, centerFrom.y + EDGE_OUT.toPx())
    val to = Offset(centerTo.x, centerTo.y - EDGE_IN.toPx())
    val mid = (from.y + to.y) / 2
    val path = Path().apply { moveTo(from.x, from.y); cubicTo(from.x, mid, to.x, mid, to.x, to.y) }
    val style = Stroke(
        width = (if (walked) 8.dp else 6.dp).toPx(), cap = StrokeCap.Round,
        pathEffect = if (walked) null else PathEffect.dashPathEffect(floatArrayOf(0.1f, 20.dp.toPx()))
    )
    drawPath(path, if (walked) accent else track, style = style)
}

/** Остановка на тропе или в графе: узел с центром в точке (`x`, `y`) и название под ним. `dim` — тема пока далеко (не открыта). */
@Composable
internal fun Stop(topic: Topic, number: Int, stages: Int, here: Boolean, dim: Boolean, x: Dp, y: Dp, width: Dp, onOpen: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val accent = Color(LocalAccent.current.main)
    Column(Modifier.width(width).offset(x - width / 2, y - 38.dp).alpha(if (dim) 0.72f else 1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(76.dp), Alignment.Center) { Node(number, stages, here) { onOpen(topic.id) } }
        Text(
            topic.title, Modifier.padding(top = 6.dp).height(LABEL_HEIGHT), if (here) accent else colors.onBackground,
            fontWeight = if (here) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center, maxLines = 2
        )
    }
}

/** Узел: пройденный — залитый круг с галочкой, текущий — кольцо этапов со свечением, дальний — номер. */
@Composable
private fun Node(number: Int, stages: Int, here: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val tap = Modifier.clip(CircleShape).clickable(role = Role.Button, onClick = onClick)
    when {
        stages == 3 -> Box(
            Modifier.size(68.dp).then(tap).background(Brush.linearGradient(LocalAccent.current.gradientColors())),
            contentAlignment = Alignment.Center
        ) { AppIcon(R.drawable.ic_check, Color.White, 36.dp) }
        here -> {
            val pulse by rememberInfiniteTransition().animateFloat(1f, 1.28f, infiniteRepeatable(tween(1100), RepeatMode.Reverse))
            Box(Modifier.size(76.dp), Alignment.Center) {
                Box(Modifier.size(76.dp).graphicsLayer { scaleX = pulse; scaleY = pulse }.background(colors.primary.copy(alpha = 0.18f), CircleShape))
                Ring(stages / 3f, 76.dp, 7.dp, tap) {
                    Box(Modifier.size(54.dp).background(colors.primaryContainer, CircleShape), Alignment.Center) { AppIcon(R.drawable.ic_play, colors.primary, 30.dp) }
                }
            }
        }
        else -> Box(
            Modifier.size(60.dp).then(tap).background(colors.surfaceVariant).border(1.dp, colors.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text("$number", style = MaterialTheme.typography.titleMedium, color = colors.onSurfaceVariant) }
    }
}
