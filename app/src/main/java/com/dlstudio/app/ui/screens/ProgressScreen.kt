package com.dlstudio.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.content.Topic
import com.dlstudio.app.progress.TopicProgress
import com.dlstudio.app.progress.Xp
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.components.AppIcon
import com.dlstudio.app.ui.components.Pill
import com.dlstudio.app.ui.components.ProgressBar
import com.dlstudio.app.ui.components.Ring
import com.dlstudio.app.ui.components.ScreenHeader
import com.dlstudio.app.ui.components.SegmentedTabs
import com.dlstudio.app.ui.components.SettingCard
import com.dlstudio.app.ui.components.SettingsSection
import com.dlstudio.app.ui.theme.LocalStrings

/** Прогресс: две вкладки — «Обзор» (уровень и XP, серия дней, пройденные темы и «печати» групп) и «Статистика» (см. StatsContent). */
@Composable
fun ProgressScreen(state: AppState) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    var tab by rememberSaveable { mutableStateOf(0) }
    val xp = state.progress.xp
    val done = state.topics.count { Xp.complete(it, state.progress.topics[it.id] ?: TopicProgress()) }
    val seals = state.topics.groupBy { it.course to it.group }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), Arrangement.spacedBy(12.dp)) {
        ScreenHeader(s.tabProgress)
        SegmentedTabs(listOf(s.tabOverview, s.tabStats), tab, { tab = it })
        if (tab == 1) {
            StatsContent(state)
            return@Column
        }
        Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Alignment.CenterHorizontally) {
            Ring(Xp.intoLevel(xp) / Xp.LEVEL.toFloat(), 168.dp, 14.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${Xp.level(xp)}", style = MaterialTheme.typography.displayMedium)
                    Text("$xp XP", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
            }
            Text(s.xpToNext(Xp.LEVEL - Xp.intoLevel(xp)), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(s.streakDays(Xp.currentStreak(state.progress, Xp.dayNumber())), accent = true)
                Pill(s.topicsDone(done, state.topics.size))
            }
        }
        if (seals.isNotEmpty()) SettingsSection(s.sealsTitle) {
            seals.forEach { (key, topics) -> Seal(key.first, key.second.ifBlank { s.noGroup }, topics, state) }
        }
        SettingsSection(s.xpRulesTitle) {
            SettingCard { Text(s.xpRules, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Start) }
        }
    }
}

@Composable
private fun Seal(course: String, group: String, topics: List<Topic>, state: AppState) {
    val s = LocalStrings.current
    val done = topics.count { Xp.complete(it, state.progress.topics[it.id] ?: TopicProgress()) }
    SettingCard(gap = 10.dp) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(listOf(course, group).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { s.noGroup }, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            if (done == topics.size) AppIcon(R.drawable.ic_check, MaterialTheme.colorScheme.primary, 24.dp)
            else Text(s.groupProgress(done, topics.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        ProgressBar(done / topics.size.toFloat())
    }
}
