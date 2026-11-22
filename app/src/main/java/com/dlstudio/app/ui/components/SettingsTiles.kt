package com.dlstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dlstudio.app.BuildConfig
import com.dlstudio.app.R
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.ui.theme.LocalStrings

/** Раздел настроек: небольшой акцентный заголовок и плитки под ним. Все разделы выглядят одинаково. */
@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title.uppercase(), Modifier.padding(start = 6.dp, top = 8.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        content()
    }
}

/**
 * Единая плитка: контур, фон, скругление и отступы одни для всех видов плиток.
 * `onClick` делает нажимаемой всю плитку; `gap` — расстояние между строками внутри.
 */
@Composable
fun SettingCard(
    modifier: Modifier = Modifier.fillMaxWidth(),
    onClick: (() -> Unit)? = null,
    gap: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)
    val tap = if (onClick == null) Modifier else Modifier.clip(shape).clickable(role = Role.Button, onClick = onClick)
    Surface(modifier.then(tap), shape, colors.surfaceVariant, colors.onSurface, border = BorderStroke(1.dp, colors.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), Arrangement.spacedBy(gap), content = content)
    }
}

/** Плитка: название сверху, значение под ним (после «/» разрешён перенос); тап — действие. */
@Composable
fun SettingTile(title: String, value: String, onClick: () -> Unit) {
    SettingCard(onClick = onClick, gap = 3.dp) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(value.replace("/", "/\u200b"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, maxLines = 2)
    }
}

/** Плитка-кнопка с одной подписью по центру: короткое действие рядом с другими в `SettingRow`. */
@Composable
fun ActionTile(label: String, onClick: () -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    SettingCard(modifier, onClick) {
        Text(label, Modifier.fillMaxWidth(), MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}

/**
 * Плитка-кнопка: показывает текущее значение, каждое нажатие переключает на следующее по кругу (после последнего — снова первое).
 * В ряд ставится через `SettingRow` и `Modifier.weight(1f)`.
 */
@Composable
fun <T> CycleTile(title: String, options: List<Pair<String, T>>, selected: T, onSelect: (T) -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    val colors = MaterialTheme.colorScheme
    val index = options.indexOfFirst { it.second == selected }
    SettingCard(modifier, { onSelect(options[(index + 1) % options.size].second) }) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f), Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(options.getOrNull(index)?.first.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = colors.primary, maxLines = 1)
            }
            AppIcon(R.drawable.ic_cycle, colors.primary, 22.dp)
        }
    }
}

/** Ряд из двух-трёх плиток одинаковой ширины (каждой передаётся `Modifier.weight(1f)`). */
@Composable
fun SettingRow(content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp), content = content)
}

/** Плитка «Акцентный цвет»: радужная линейка — тап или перетаскивание выбирает любой оттенок. */
@Composable
fun AccentTile(title: String, selected: AccentColor, onSelect: (AccentColor) -> Unit) {
    SettingCard(gap = 14.dp) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        RainbowBar(selected, onSelect)
    }
}

/** Линейка всех оттенков; ползунок стоит на выбранном и залит этим цветом. */
@Composable
private fun RainbowBar(selected: AccentColor, onSelect: (AccentColor) -> Unit) {
    val rainbow = remember { Brush.horizontalGradient((0..12).map { Color.hsv(it * 30f, 0.75f, 1f) }) }
    val thumb = 40.dp
    BoxWithConstraints(Modifier.fillMaxWidth().height(thumb)) {
        val track = maxWidth - thumb
        Box(
            Modifier.fillMaxSize().padding(horizontal = thumb / 2, vertical = 8.dp).clip(RoundedCornerShape(12.dp)).background(rainbow)
                .pointerInput(Unit) { detectTapGestures { onSelect(AccentColor(hueAt(it.x, size.width.toFloat()))) } }
                .pointerInput(Unit) { detectHorizontalDragGestures { change, _ -> change.consume(); onSelect(AccentColor(hueAt(change.position.x, size.width.toFloat()))) } }
        )
        Box(
            Modifier.offset(x = track * (selected.hue / 360f)).size(thumb).background(Color(selected.main), CircleShape)
                .border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
        )
    }
}

/** Оттенок под пальцем: край линейки — красный, середина — голубой. */
private fun hueAt(x: Float, width: Float): Float = (x / width).coerceIn(0f, 1f) * 360f

/** Постоянная карточка о приложении и компании в настройках. */
@Composable
fun AboutCard() {
    val s = LocalStrings.current
    SettingCard(gap = 6.dp) {
        Text(s.aboutTitle, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text("DataLaw DesignLab", style = MaterialTheme.typography.titleMedium)
        Text(s.aboutText, style = MaterialTheme.typography.bodyMedium)
        Text("v${BuildConfig.VERSION_NAME} · ${s.aboutCopyright}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
