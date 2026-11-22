package com.dlstudio.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.i18n.Strings
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.gradientColors

/** Нижняя панель: градиентные иконки в «капсуле» с тонким контуром. */
@Composable
fun BottomTabs(selected: Int, onSelect: (Int) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Surface(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), RoundedCornerShape(30.dp),
            MaterialTheme.colorScheme.surfaceVariant, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(Modifier.padding(6.dp)) {
                Tabs.all.forEachIndexed { index, tab -> TabItem(tab, selected == index, { onSelect(index) }, Modifier.weight(1f)) }
            }
        }
    }
}

/** Цветная иконка нижней навигации. Анимация читается только на этапах слоя и рисования — без перекомпоновок на каждый кадр. */
@Composable
private fun TabIcon(tab: TabSpec, selected: Boolean) {
    val accent = LocalAccent.current.gradientColors()
    val k by animateFloatAsState(if (selected) 1f else 0f, spring(stiffness = Spring.StiffnessMediumLow), label = "tab")
    val shape = RoundedCornerShape(21.dp)
    val glow = if (selected) Modifier.shadow(14.dp, shape, ambientColor = accent.last(), spotColor = accent.last()) else Modifier
    Box(
        Modifier.size(56.dp)
            .graphicsLayer { val scale = 1f + 0.08f * k; scaleX = scale; scaleY = scale; alpha = 0.82f + 0.18f * k }
            .then(glow)
            .drawBehind { drawRoundRect(Brush.linearGradient(accent), cornerRadius = CornerRadius(21.dp.toPx()), alpha = 0.28f + 0.72f * k) },
        contentAlignment = Alignment.Center
    ) { AppIcon(if (selected) tab.fill else tab.outline, Color.White, 32.dp) }
}

/** Одна вкладка: иконка и подпись в одну строку. */
@Composable
private fun TabItem(tab: TabSpec, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier.selectable(selected, remember { MutableInteractionSource() }, null, role = Role.Tab, onClick = onClick).padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TabIcon(tab, selected)
        Text(
            tab.label(LocalStrings.current), Modifier.padding(top = 4.dp), if (selected) colors.onBackground else colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.Center), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

/** Описание вкладки: подпись и иконка в выбранном (залитая) и обычном (контурная) виде. */
class TabSpec(val label: (Strings) -> String, @DrawableRes val fill: Int, @DrawableRes val outline: Int)

/** Четыре вкладки по порядку свайпа: Старт → Карта → Прогресс → Настройки (иконки Material). */
object Tabs {
    val all = listOf(
        TabSpec(Strings::tabStart, R.drawable.ic_tab_home_fill, R.drawable.ic_tab_home_outline),
        TabSpec(Strings::tabMap, R.drawable.ic_tab_library_fill, R.drawable.ic_tab_library_outline),
        TabSpec(Strings::tabProgress, R.drawable.ic_tab_stats_fill, R.drawable.ic_tab_stats_outline),
        TabSpec(Strings::tabSettings, R.drawable.ic_tab_settings_fill, R.drawable.ic_tab_settings_outline)
    )
}
