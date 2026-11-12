package com.dlstudio.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.study.Speak
import com.dlstudio.app.ui.components.ActionIconButton
import com.dlstudio.app.ui.components.AppIcon
import com.dlstudio.app.ui.components.FitText
import com.dlstudio.app.ui.components.SmallSpeaker
import com.dlstudio.app.ui.components.StudyPlayButton
import com.dlstudio.app.ui.components.capFirst
import com.dlstudio.app.ui.theme.LocalStrings

/** Плитка сетки: крупная иконка уровня, меню в углу, название и подпись внизу; у карточки ещё озвучка слова. */
@Composable
fun ItemTile(node: LibNode, level: Int, actions: ItemActions) {
    Column(
        Modifier.fillMaxWidth().height(if (level == 2) 216.dp else 184.dp).clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            .clickable { actions.onOpen(node) }.padding(start = 14.dp, top = 14.dp, end = 4.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
            ItemBadge(level, 52.dp)
            ItemMenu(node, actions)
        }
        Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
                ItemText(node, level, Modifier.weight(1f).padding(end = 10.dp))
                if (cards(node, level) > 0) StudyPlayButton({ actions.onStudy(node) }, Modifier.padding(end = 10.dp))
            }
            if (level == 2) SmallSpeaker(node.name, actions.onSpeak)
        }
    }
}

/** Строка списка: плитка уровня, название с подписью (у карточки под ними озвучка), меню. */
@Composable
fun ItemRow(node: LibNode, level: Int, actions: ItemActions) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .clickable { actions.onOpen(node) }.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ItemBadge(level, 48.dp)
        Column(Modifier.weight(1f), Arrangement.spacedBy(6.dp)) {
            ItemText(node, level)
            if (level == 2) SmallSpeaker(node.name, actions.onSpeak)
        }
        if (cards(node, level) > 0) StudyPlayButton({ actions.onStudy(node) })
        ItemMenu(node, actions)
    }
}

/** Название элемента и подпись: для проекта и колоды — сколько внутри, для карточки — обратная сторона. */
@Composable
fun ItemText(node: LibNode, level: Int, modifier: Modifier = Modifier) {
    val sub = if (level == 2) node.back else LocalStrings.current.contentCount(level, node.children.size)
    Column(modifier) {
        FitText(node.name.capFirst(), MaterialTheme.typography.titleMedium, Modifier.fillMaxWidth(), maxLines = 2)
        FitText(
            sub.capFirst(), MaterialTheme.typography.bodyMedium, Modifier.fillMaxWidth().padding(top = 2.dp),
            MaterialTheme.colorScheme.onSurfaceVariant, 2
        )
    }
}

/** Всегда видимые действия карточки через компонент ActionIconButton: карандаш и корзина. */
@Composable
fun ItemMenu(node: LibNode, actions: ItemActions) {
    val s = LocalStrings.current
    val c = MaterialTheme.colorScheme
    Row {
        ActionIconButton(
            icon = R.drawable.ic_edit,
            onClick = { actions.onEdit(node) },
            size = 42.dp,
            iconSize = 20.dp,
            containerColor = c.surfaceVariant,
            iconTint = c.primary,
            contentDescription = s.edit
        )
        ActionIconButton(
            icon = R.drawable.ic_trash,
            onClick = { actions.onDelete(node) },
            size = 42.dp,
            iconSize = 20.dp,
            containerColor = c.surfaceVariant,
            iconTint = c.error,
            contentDescription = s.delete
        )
    }
}

/** Градиентная плитка уровня (проект, колода, карточка) с одинаковой геометрией в библиотеке и учёбе. */
@Composable
fun ItemBadge(level: Int, size: Dp) {
    Box(
        Modifier.size(size).background(Brush.linearGradient(LevelLook.colors[level]), RoundedCornerShape(size * 0.3f)),
        contentAlignment = Alignment.Center
    ) { AppIcon(LevelLook.icons[level], Color.White, size * 0.56f) }
}

/** Что можно сделать с элементом: открыть (тап), изменить, удалить, учить (проект или колоду целиком), озвучить слово карточки. */
class ItemActions(
    val onOpen: (LibNode) -> Unit,
    val onEdit: (LibNode) -> Unit,
    val onDelete: (LibNode) -> Unit,
    val onStudy: (LibNode) -> Unit,
    val onSpeak: Speak
)

/** Оформление уровней: 0 — проект, 1 — колода, 2 — карточка (градиент плитки и иконка). */
object LevelLook {
    val colors = listOf(
        listOf(Color(0xFFA77BFF), Color(0xFFFF4FD8)),
        listOf(Color(0xFFFFB02E), Color(0xFFFF3D77)),
        listOf(Color(0xFF2EE6FF), Color(0xFF4A5BFF))
    )
    val icons = listOf(R.drawable.ic_lv_project, R.drawable.ic_lv_deck, R.drawable.ic_lv_card)
}

/** Сколько карточек лежит в проекте (уровень 0) или колоде (уровень 1); у самой карточки — 0. */
fun cards(node: LibNode, level: Int): Int = when (level) {
    0 -> node.children.sumOf { it.children.size }
    1 -> node.children.size
    else -> 0
}
