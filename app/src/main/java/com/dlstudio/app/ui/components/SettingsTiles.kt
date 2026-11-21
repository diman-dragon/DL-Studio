package com.dlstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dlstudio.app.BuildConfig
import com.dlstudio.app.R
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AccentPalette
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
    Surface(modifier.then(tap), shape, colors.surfaceVariant, border = BorderStroke(1.dp, colors.outlineVariant)) {
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

/** Плитка «Акцентный цвет»: палитры, ряд образцов и название выбранного цвета. */
@Composable
fun AccentTile(title: String, selected: AccentColor, name: (AccentColor) -> String, onSelect: (AccentColor) -> Unit) {
    SettingCard(gap = 12.dp) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        AccentWheel(selected, onSelect)
        Text(name(selected), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun AccentWheel(selected: AccentColor, onSelect: (AccentColor) -> Unit) {
    val strings = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val current = AccentPalette.forAccent(selected)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            AccentPalette.values().forEach { palette ->
                val active = palette == current
                Surface(
                    Modifier.weight(1f).clickable(role = Role.RadioButton) { onSelect(palette.colors[current.slot(selected)]) }, RoundedCornerShape(12.dp),
                    if (active) colors.primaryContainer else colors.background, border = BorderStroke(1.dp, if (active) colors.primary else colors.outline)
                ) {
                    Box(Modifier.padding(vertical = 8.dp), Alignment.Center) {
                        Text(strings.accentPaletteName(palette), style = MaterialTheme.typography.labelMedium, color = if (active) colors.primary else colors.onSurfaceVariant)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly, Alignment.Top) {
            current.colors.forEach { accent ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(accent.main), Color(accent.deep))))
                            .then(if (accent == selected) Modifier.border(3.dp, colors.onSurface, CircleShape) else Modifier)
                            .clickable(role = Role.RadioButton) { onSelect(accent) }.semantics { this.selected = accent == selected }
                    )
                    if (accent == selected) Text(strings.accentName(accent), style = MaterialTheme.typography.labelSmall, color = colors.primary)
                }
            }
        }
    }
}

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
