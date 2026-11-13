package com.dlstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dlstudio.app.BuildConfig
import com.dlstudio.app.R
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AccentPalette
import com.dlstudio.app.settings.FontChoice
import com.dlstudio.app.settings.FontGroup
import com.dlstudio.app.settings.FontSpec
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.TextRole

/**
 * Единая плитка настроек: контур, фон, скругление и отступы одни для всех видов плиток.
 * `onClick` делает нажимаемой всю плитку; `gap` — расстояние между строками внутри.
 */
@Composable
fun SettingCard(
    modifier: Modifier = Modifier.fillMaxWidth(),
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    gap: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = CardStyle.shape
    val tap = if (onClick == null) Modifier else Modifier.clip(shape).clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    Surface(modifier.then(tap), shape, colors.surfaceVariant, border = BorderStroke(1.dp, colors.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), Arrangement.spacedBy(gap), content = content)
    }
}

/** Плитка настроек: название сверху, значение под ним (после «/» разрешён перенос); тап — действие. */
@Composable
fun SettingTile(title: String, value: String, onClick: () -> Unit, enabled: Boolean = true) {
    SettingCard(onClick = onClick, enabled = enabled, gap = 3.dp) {
        FitText(title, MaterialTheme.typography.titleMedium, Modifier.fillMaxWidth())
        FitText(value.replace("/", "/\u200b"), MaterialTheme.typography.bodyMedium, Modifier.fillMaxWidth(), MaterialTheme.colorScheme.primary, 2)
    }
}

/**
 * Плитка-кнопка: показывает текущее значение, каждое нажатие переключает на следующее по кругу (после последнего — снова первое).
 * Если текущего значения нет в списке, первое нажатие даёт первое значение. В ряд ставится через `SettingRow` и `Modifier.weight(1f)`.
 */
@Composable
fun <T> CycleTile(title: String, options: List<Pair<String, T>>, selected: T, onSelect: (T) -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    val colors = MaterialTheme.colorScheme
    val index = options.indexOfFirst { it.second == selected }
    SettingCard(modifier, { onSelect(options[(index + 1) % options.size].second) }) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f), Arrangement.spacedBy(3.dp)) {
                FitText(title, MaterialTheme.typography.titleMedium, Modifier.fillMaxWidth())
                FitText(options.getOrNull(index)?.first.orEmpty(), MaterialTheme.typography.bodyMedium, Modifier.fillMaxWidth(), colors.primary)
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

/** Плитка с переключателем: название, пояснение и Switch; нажимается вся плитка (`enabled = false` — переключатель заблокирован). */
@Composable
fun SwitchTile(title: String, value: String, checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true) {
    SettingCard(Modifier.fillMaxWidth().toggleable(checked, enabled, Role.Switch, onChange)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(Modifier.weight(1f), Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Switch(checked, null, enabled = enabled)
        }
    }
}

/** Плитка «Акцентный цвет»: ряд образцов и название выбранного цвета под ним. */
@Composable
fun AccentTile(title: String, selected: AccentColor, name: (AccentColor) -> String, onSelect: (AccentColor) -> Unit) {
    SettingCard(gap = 12.dp) {
        FitText(title, MaterialTheme.typography.titleMedium, Modifier.fillMaxWidth())
        AccentWheel(selected, onSelect)
        FitText(name(selected), MaterialTheme.typography.bodyMedium, Modifier.fillMaxWidth(), MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun AccentWheel(selected: AccentColor, onSelect: (AccentColor) -> Unit) {
    val strings = LocalStrings.current
    val current = AccentPalette.forAccent(selected)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            AccentPalette.values().forEach { palette ->
                val active = palette == current
                Surface(Modifier.weight(1f).clickable(role = Role.RadioButton) { onSelect(palette.colors[current.slot(selected)]) }, RoundedCornerShape(12.dp), if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background, border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)) {
                    Box(Modifier.padding(vertical = 8.dp), Alignment.Center) {
                        Text(strings.accentPaletteName(palette), style = MaterialTheme.typography.labelMedium, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly, Alignment.Top) {
            current.colors.forEach { accent ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(48.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(accent.main), Color(accent.deep)))).then(if (accent == selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier).clickable(role = Role.RadioButton) { onSelect(accent) }.semantics { this.selected = accent == selected })
                    if (accent == selected) Text(strings.accentName(accent), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

/** Плитка выбора из нескольких значений, которые удобно видеть сразу: название и ряд чипов, выбранный подсвечен акцентом. */
@Composable
fun <T> OptionChips(title: String, options: List<Pair<String, T>>, selected: T, onSelect: (T) -> Unit) {
    val colors = MaterialTheme.colorScheme
    SettingCard(gap = 10.dp) {
        FitText(title, MaterialTheme.typography.titleMedium, Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            options.forEach { (label, value) ->
                val on = value == selected
                Surface(
                    Modifier.weight(1f).clickable(role = Role.RadioButton) { onSelect(value) }, RoundedCornerShape(14.dp),
                    if (on) colors.primaryContainer else colors.background, border = BorderStroke(1.dp, if (on) colors.primary else colors.outline)
                ) {
                    Box(Modifier.height(46.dp), Alignment.Center) {
                        ButtonLabel(label, if (on) colors.primary else colors.onSurfaceVariant, Modifier.fillMaxHeight(0.9f), role = TextRole.BUTTON_CHIP, maxLines = 1)
                    }
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

/** Настройка одной группы текста: название, образец и две плитки-кнопки в ряд — шрифт и размер. */
@Composable
fun FontTile(group: FontGroup, spec: FontSpec, onChange: (FontSpec) -> Unit) {
    val s = LocalStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(s.fontGroupName(group), Modifier.padding(start = 6.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        GroupText(s.fontSample, group, Modifier.padding(horizontal = 6.dp), MaterialTheme.colorScheme.onSurface, MaterialTheme.typography.titleLarge)
        SettingRow {
            CycleTile(s.fontFamilyLabel, FontChoice.values().map { s.fontChoiceName(it) to it }, spec.choice, { onChange(spec.copy(choice = it)) }, Modifier.weight(1f))
            CycleTile(s.fontSizeLabel, FontSpec.SCALES.map { "$it%" to it }, spec.scale, { onChange(spec.copy(scale = it)) }, Modifier.weight(1f))
        }
    }
}
