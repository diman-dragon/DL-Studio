package com.dlstudio.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

/** Тема в списке: `course` → `group` (урок) → сама тема. Пустые `course` и `group` — «Без курса» и тема сразу под курсом. */
data class TreeRow(val id: String, val course: String, val group: String, val title: String, val busy: Boolean = false)

private const val INDENT_DP = 14

/**
 * Многоуровневый список: курс → урок → тема. Курсы и уроки раскрываются по нажатию (если курс один, он раскрыт сразу).
 * У курса и урока справа — действие над всеми темами внутри (`manyLabel`), у темы — своё (`rowLabel`).
 * `scope` отличает состояние раскрытия разных списков на одном экране.
 */
@Composable
fun TopicTree(scope: String, rows: List<TreeRow>, rowLabel: String, manyLabel: String, onRow: (TreeRow) -> Unit, onMany: (List<TreeRow>) -> Unit) {
    val s = LocalStrings.current
    val open = remember { mutableStateMapOf<String, Boolean>() }
    val byCourse = rows.groupBy { it.course }
    byCourse.forEach { (course, inCourse) ->
        val courseKey = "$scope|$course"
        val courseOpen = open[courseKey] ?: (byCourse.size == 1)
        TreeHeader(course.ifBlank { s.noCourse }, inCourse.size, courseOpen, 0, manyLabel, { open[courseKey] = !courseOpen }, { onMany(inCourse) })
        if (courseOpen) {
            inCourse.groupBy { it.group }.forEach { (group, inGroup) ->
                if (group.isBlank()) {
                    inGroup.forEach { TopicRow(it.title, "", rowLabel, it.busy, 1) { onRow(it) } }
                } else {
                    val groupKey = "$courseKey|$group"
                    val groupOpen = open[groupKey] ?: true
                    TreeHeader(group, inGroup.size, groupOpen, 1, manyLabel, { open[groupKey] = !groupOpen }, { onMany(inGroup) })
                    if (groupOpen) inGroup.forEach { TopicRow(it.title, "", rowLabel, it.busy, 2) { onRow(it) } }
                }
            }
        }
    }
}

/** Заголовок курса или урока: стрелка раскрытия, название, число тем и действие над всеми темами. */
@Composable
private fun TreeHeader(title: String, count: Int, expanded: Boolean, level: Int, action: String, onToggle: () -> Unit, onAction: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    SettingCard(Modifier.fillMaxWidth().padding(start = (level * INDENT_DP).dp), onToggle, 2.dp) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Alignment.CenterVertically) {
            Text(if (expanded) "▾" else "▸", style = MaterialTheme.typography.titleMedium, color = colors.primary)
            Column(Modifier.weight(1f), Arrangement.spacedBy(2.dp)) {
                Text(title, style = if (level == 0) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium, maxLines = 2)
                Text("$count", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Text(action, Modifier.clickable(onClick = onAction).padding(6.dp), style = MaterialTheme.typography.labelLarge, color = colors.primary)
        }
    }
}

/** Строка темы: название и действие справа (при загрузке вместо него крутится индикатор). `level` задаёт отступ слева. */
@Composable
fun TopicRow(title: String, subtitle: String, action: String, busy: Boolean, level: Int = 0, onClick: () -> Unit) {
    SettingCard(Modifier.fillMaxWidth().padding(start = (level * INDENT_DP).dp), if (busy) null else onClick, 2.dp) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f), Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            else Text(action, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}
