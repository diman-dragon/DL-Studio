package com.dlstudio.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.dlstudio.app.content.Topic
import com.dlstudio.app.progress.TopicGraph
import com.dlstudio.app.progress.TopicProgress
import com.dlstudio.app.progress.Xp
import com.dlstudio.app.ui.theme.LocalAccent

/**
 * Граф курса с предпосылками: темы без предпосылок сверху, каждая следующая — слоем ниже своих предпосылок.
 * Линия от предпосылки к теме залита, когда предпосылка пройдена. Открытые к прохождению темы подсвечены («вы здесь»), закрытые приглушены,
 * но открыть можно любую.
 */
@Composable
fun TopicGraphView(topics: List<Topic>, progress: Map<String, TopicProgress>, onOpen: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val accent = Color(LocalAccent.current.main)
    val layers = TopicGraph.layers(topics)
    val rows = topics.groupBy { layers.getValue(it.id) }.toSortedMap().values.toList()
    val open = TopicGraph.open(topics, progress).map { it.id }.toSet()
    val place = HashMap<String, Pair<Int, Float>>()
    rows.forEachIndexed { r, list -> list.forEachIndexed { j, t -> place[t.id] = r to (j + 1f) / (list.size + 1) } }
    val widest = rows.maxOf { it.size }

    BoxWithConstraints(Modifier.fillMaxWidth().height(ROW * rows.size)) {
        val label = minOf(LABEL, maxWidth / (widest + 1))
        Canvas(Modifier.fillMaxSize()) {
            val row = ROW.toPx()
            topics.forEach { child ->
                val (cr, cf) = place.getValue(child.id)
                topics.filter { it.id in child.requires && it.id != child.id }.forEach { parent ->
                    val (pr, pf) = place.getValue(parent.id)
                    val walked = Xp.complete(parent, progress[parent.id] ?: TopicProgress())
                    drawEdge(Offset(pf * size.width, row * pr + row / 2), Offset(cf * size.width, row * cr + row / 2), walked, accent, colors.outline)
                }
            }
        }
        topics.forEachIndexed { i, topic ->
            val (r, f) = place.getValue(topic.id)
            val stages = Xp.stagesDone(topic, progress[topic.id] ?: TopicProgress())
            Stop(topic, i + 1, stages, topic.id in open, topic.id !in open && stages < 3, maxWidth * f, ROW * r + ROW / 2, label, onOpen)
        }
    }
}
