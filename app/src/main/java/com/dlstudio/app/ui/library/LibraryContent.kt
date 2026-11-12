package com.dlstudio.app.ui.library

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.ui.components.AppIcon
import com.dlstudio.app.ui.components.BackButton
import com.dlstudio.app.ui.components.FitText
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.gradientColors

/** Содержимое уровня: пусто → подсказка, иначе сетка или список по переключателю. */
@Composable
fun LibraryContent(nodes: List<LibNode>, level: Int, grid: Boolean, actions: ItemActions) {
    when {
        nodes.isEmpty() -> EmptyState(level)
        grid -> LibraryGrid(nodes, level, actions)
        else -> LibraryList(nodes, level, actions)
    }
}

/** Вид «список»: строки одна под другой. */
@Composable
fun LibraryList(nodes: List<LibNode>, level: Int, actions: ItemActions) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(nodes, key = { it.id }) { ItemRow(it, level, actions) }
    }
}

/** Заголовок: название открытого узла; внутри проекта или колоды компонентная кнопка «Назад». */
@Composable
fun LibraryHeader(title: String, canGoUp: Boolean, onUp: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        if (canGoUp) BackButton(onClick = onUp)
        FitText(title, MaterialTheme.typography.headlineMedium, Modifier.weight(1f))
    }
}

/** Плавающие кнопки библиотеки: сверху «импорт из файла», под ней «+» (подписи зависят от уровня). */
@Composable
fun LibraryActions(level: Int, onAdd: () -> Unit, onImport: () -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    Column(modifier, Arrangement.spacedBy(14.dp), Alignment.CenterHorizontally) {
        ImportButton(s.importTitle(level), onImport)
        AddButton(s.addTitle(level), onAdd)
    }
}

/** Пустой уровень: большая плитка и подсказка, что создать. */
@Composable
fun EmptyState(level: Int) {
    Column(Modifier.fillMaxSize().padding(32.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        ItemBadge(level, 96.dp)
        Spacer(Modifier.height(20.dp))
        Text(
            LocalStrings.current.emptyText(level),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

/** Кнопка «+» в цвете выбранного акцента; `label` читает экранный диктор. */
@Composable
fun AddButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAccent.current.gradientColors()
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier.size(62.dp)
            .shadow(14.dp, shape, ambientColor = colors.last(), spotColor = colors.last())
            .background(Brush.linearGradient(colors), shape)
            .semantics { contentDescription = label }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { AppIcon(R.drawable.ic_plus, Color.White, 30.dp) }
}

/** Малая кнопка «импорт из файла» над «+»: плитка поверхности с иконкой в цвете акцента. */
@Composable
fun ImportButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier.size(50.dp).clip(RoundedCornerShape(18.dp)).background(colors.surfaceVariant)
            .semantics { contentDescription = label }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { AppIcon(R.drawable.ic_import, colors.primary, 26.dp) }
}

/** Переключатель «Карточки / Список» под заголовком. */
@Composable
fun ViewToggle(grid: Boolean, onChange: (Boolean) -> Unit) {
    val s = LocalStrings.current
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp)
    ) {
        ToggleSegment(s.viewCards, R.drawable.ic_view_grid, grid, { onChange(true) }, Modifier.weight(1f))
        ToggleSegment(s.viewList, R.drawable.ic_view_list, !grid, { onChange(false) }, Modifier.weight(1f))
    }
}

/** Одна половина переключателя вида: иконка и подпись; выбранная заливается цветом темы. */
@Composable
fun ToggleSegment(label: String, @DrawableRes icon: Int, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (selected) c.primary else Color.Transparent, label = "segment")
    val fg = if (selected) c.onPrimary else c.onSurfaceVariant
    Row(
        modifier.clip(RoundedCornerShape(14.dp)).background(bg)
            .selectable(selected, role = Role.RadioButton, onClick = onClick).padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(icon, fg, 20.dp)
        Spacer(Modifier.width(8.dp))
        FitText(label, MaterialTheme.typography.labelLarge, color = fg)
    }
}
