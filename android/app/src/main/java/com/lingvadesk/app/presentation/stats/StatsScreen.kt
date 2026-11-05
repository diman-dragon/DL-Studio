package com.lingvadesk.app.presentation.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.lingvadesk.app.domain.model.StatsSummary
import com.lingvadesk.app.domain.repository.LingvaRepository
import com.lingvadesk.app.presentation.common.components.ItemCard
import com.lingvadesk.app.presentation.common.components.ScreenTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(private val repo: LingvaRepository) : ViewModel() {
    private val _ui = MutableStateFlow<StatsSummary?>(null)
    val ui: StateFlow<StatsSummary?> = _ui.asStateFlow()
    fun reload() { viewModelScope.launch { _ui.value = repo.stats(System.currentTimeMillis()) } }
}

@Composable
fun StatsScreen(vm: StatsViewModel = hiltViewModel()) {
    val s by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.reload() }
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Статистика")
        val st = s ?: return@Column
        Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ItemCard("Сегодня", "${st.today.n} повторений · серия ${st.today.current} дн. · лучшая ${st.today.best} дн.", onClick = {})
            ItemCard("Карточки", "всего ${st.total} · новых ${st.fresh} · к повторению ${st.due} · выучено ${st.mature}", onClick = {})
            ItemCard("Повторений всего", st.reviewsTotal.toString(), onClick = {})
            ItemCard("Удержание за 30 дней", st.retention30?.let { "$it %" } ?: "нет данных", onClick = {})
            ItemCard("Произношение за 30 дней", st.avgPronunciation?.let { "$it %" } ?: "нет данных", onClick = {})
            Text("Повторения по дням", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            val max = (st.last7.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
            Row(Modifier.fillMaxWidth().height(140.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                st.last7.forEach { d ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Text(d.count.toString(), style = MaterialTheme.typography.labelSmall)
                        Box(Modifier.fillMaxWidth().height((90f * d.count / max).dp.coerceAtLeast(2.dp)).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
                        Text(d.label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
