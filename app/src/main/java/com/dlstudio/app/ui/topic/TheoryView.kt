package com.dlstudio.app.ui.topic

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.content.Block
import com.dlstudio.app.content.CalloutKind
import com.dlstudio.app.content.CourseCast
import com.dlstudio.app.content.TheoryBlocks
import com.dlstudio.app.content.TheoryPage
import com.dlstudio.app.content.Tile
import com.dlstudio.app.content.TileKind
import com.dlstudio.app.speech.CharRole
import com.dlstudio.app.speech.Characters
import com.dlstudio.app.speech.Speak
import com.dlstudio.app.speech.Utter
import com.dlstudio.app.ui.components.CharacterAvatar
import com.dlstudio.app.ui.components.LocalSpeaking
import com.dlstudio.app.ui.components.Markup
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.gradientColors
import java.io.File

private val VOWEL_COLOR = Color(0xFFFF7A59)
private val CONSONANT_COLOR = Color(0xFF4C8DFF)
private const val TILES_PER_ROW = 4
private const val TILE_ALPHA = 0.2f

/**
 * Карточка теории: яркая шапка со значком, заголовком и портретом того, кто сейчас говорит на странице (пока никто не говорит — ведущего: первый преподаватель
 * среди говорящих, иначе мисс Грейс), под ней блоки страницы (текст, выноски, реплики персонажей, таблицы, плитки-буквы). Персонажи, которых ученик ещё
 * не встречал, представляются прямо перед первой своей репликой; над диалогом из нескольких реплик — кнопка «Слушать диалог».
 * `pageIndex` — номер страницы (для меток строк), `talk` — голоса, язык и знакомства.
 */
@Composable
fun TheoryCard(page: TheoryPage, pageIndex: Int, talk: Talk, mediaDir: File?, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(28.dp)
    val blocks = remember(page) { TheoryBlocks.parse(page.paragraphs) }
    val fresh = remember(page) { CourseCast.fresh(page, talk.met) }
    LaunchedEffect(page) { fresh.forEach(talk.onMeet) }
    Column(
        modifier.fillMaxWidth().shadow(14.dp, shape, ambientColor = colors.primary, spotColor = colors.primary).clip(shape)
            .background(colors.surfaceVariant).border(1.dp, colors.primary.copy(alpha = 0.35f), shape)
    ) {
        if (page.heading.isNotBlank() || page.icon.isNotBlank()) CardHeader(page)
        Column(Modifier.fillMaxWidth().padding(18.dp), Arrangement.spacedBy(14.dp)) {
            val introduced = HashSet<String>()
            blocks.forEachIndexed { i, block ->
                if (block is Block.Say) {
                    if (talk.voiced && (i == 0 || blocks[i - 1] !is Block.Say)) DialogueOf(blocks, i, pageIndex, talk)
                    val id = Characters.of(block.who).id
                    if (id in fresh && introduced.add(id)) MeetBanner(id, talk)
                }
                BlockView(block, lineId(pageIndex, i), talk, mediaDir)
            }
        }
    }
}

/** Кнопка перед подряд идущими репликами: «Слушать диалог», если говорят хотя бы двое, иначе (список слов и выражений) — «Воспроизвести все». */
@Composable
private fun DialogueOf(blocks: List<Block>, start: Int, pageIndex: Int, talk: Talk) {
    val run = blocks.drop(start).takeWhile { it is Block.Say }.filterIsInstance<Block.Say>()
    val ids = run.map { Characters.of(it.who).id }.distinct()
    if (run.size < 2) return
    val items = run.mapIndexed { j, say -> Utter(say.say.ifBlank { say.text }, say.who, lineId(pageIndex, start + j)) }
    if (ids.size < 2) DialogueBar(emptyList(), items, talk, LocalStrings.current.listenAll) else DialogueBar(ids, items, talk)
}

@Composable
private fun CardHeader(page: TheoryPage) {
    val speakers = remember(page) { (CourseCast.onPage(page) + Characters.DEFAULT).distinct() }
    val lead = remember(page) { speakers.firstOrNull { Characters.of(it).role == CharRole.TEACHER } ?: speakers.first() }
    val now = LocalSpeaking.current?.characterId
    Row(
        Modifier.fillMaxWidth().background(Brush.linearGradient(LocalAccent.current.gradientColors())).padding(horizontal = 18.dp, vertical = 16.dp),
        Arrangement.spacedBy(14.dp), Alignment.CenterVertically
    ) {
        if (page.icon.isNotBlank()) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.22f)), Alignment.Center) {
                Text(page.icon, fontSize = 28.sp)
            }
        }
        Text(page.heading, Modifier.weight(1f), Color.White, style = MaterialTheme.typography.headlineSmall)
        CharacterAvatar(now?.takeIf { it in speakers } ?: lead, 60.dp)
    }
}

@Composable
private fun BlockView(block: Block, line: String, talk: Talk, mediaDir: File?) {
    val colors = MaterialTheme.colorScheme
    when (block) {
        is Block.Text -> Text(Markup.annotate(block.text), style = MaterialTheme.typography.bodyLarge)
        is Block.Sub -> Text(block.text, style = MaterialTheme.typography.titleMedium, color = colors.primary)
        is Block.Bullets -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            block.items.forEach { item ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.padding(top = 9.dp).size(8.dp).background(colors.primary, CircleShape))
                    Text(Markup.annotate(item), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        is Block.Callout -> CalloutBox(block.kind, block.text)
        is Block.Say -> SayRow(block, line, talk)
        is Block.Table -> TableView(block.rows)
        is Block.Tiles -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (talk.voiced && block.tiles.size > 1) DialogueBar(emptyList(), block.tiles.mapIndexed { j, t -> Utter(t.say.ifBlank { t.big }, null, "$line.$j") }, talk, LocalStrings.current.listenAll)
            TileGrid(block.tiles, if (talk.voiced) { { text: String -> talk.say(text, null, null) } } else null)
        }
        is Block.Media -> MediaView(block, mediaDir)
    }
}

/** Цветная выноска со значком: совет, заметка, предупреждение или интересный факт. */
@Composable
private fun CalloutBox(kind: CalloutKind, text: String) {
    val (icon, color) = when (kind) {
        CalloutKind.TIP -> "💡" to Color(0xFFFFC857)
        CalloutKind.NOTE -> "📌" to MaterialTheme.colorScheme.primary
        CalloutKind.WARN -> "⚠️" to Color(0xFFFF6B6B)
        CalloutKind.FUN -> "✨" to Color(0xFF34D399)
    }
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(color.copy(alpha = 0.14f)).border(1.dp, color.copy(alpha = 0.55f), shape).padding(14.dp),
        Arrangement.spacedBy(12.dp)
    ) {
        Text(icon, fontSize = 22.sp)
        Text(Markup.annotate(text), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
    }
}

/** Таблица: первая строка — шапка с заливкой, дальше строки через одну тоном светлее. */
@Composable
private fun TableView(rows: List<List<String>>) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    Column(Modifier.fillMaxWidth().clip(shape).border(1.dp, colors.outlineVariant, shape)) {
        rows.forEachIndexed { i, row ->
            val fill = when { i == 0 -> colors.primaryContainer; i % 2 == 0 -> colors.surfaceContainerLow; else -> Color.Transparent }
            Row(Modifier.fillMaxWidth().background(fill).padding(horizontal = 12.dp, vertical = 10.dp), Arrangement.spacedBy(8.dp)) {
                row.forEach { cell ->
                    Text(Markup.annotate(cell), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = if (i == 0) FontWeight.Bold else null)
                }
            }
        }
    }
}

/** Сетка плиток-букв по четыре в ряд; гласные — тёплые, согласные — холодные, «и та и другая» — пополам. Тап по плитке озвучивает её. */
@Composable
private fun TileGrid(tiles: List<Tile>, speak: Speak?) {
    val s = LocalStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.chunked(TILES_PER_ROW).forEach { row ->
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                row.forEach { TileView(it, speak, Modifier.weight(1f)) }
                repeat(TILES_PER_ROW - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(14.dp), Alignment.CenterVertically) {
            if (tiles.any { it.kind != TileKind.CONSONANT }) LegendDot(VOWEL_COLOR, s.tileVowel)
            if (tiles.any { it.kind != TileKind.VOWEL }) LegendDot(CONSONANT_COLOR, s.tileConsonant)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(color, RoundedCornerShape(4.dp)))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TileView(tile: Tile, speak: Speak?, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    val warm = VOWEL_COLOR.copy(alpha = TILE_ALPHA)
    val cold = CONSONANT_COLOR.copy(alpha = TILE_ALPHA)
    val (fill, line) = when (tile.kind) {
        TileKind.VOWEL -> Brush.horizontalGradient(listOf(warm, warm)) to VOWEL_COLOR
        TileKind.CONSONANT -> Brush.horizontalGradient(listOf(cold, cold)) to CONSONANT_COLOR
        TileKind.BOTH -> Brush.horizontalGradient(0f to warm, 0.5f to warm, 0.5f to cold, 1f to cold) to colors.outline
    }
    val tap = if (speak == null) Modifier else Modifier.clickable(role = Role.Button) { speak(tile.say.ifBlank { tile.big }) }
    Column(
        modifier.clip(shape).background(fill).border(1.dp, line.copy(alpha = 0.6f), shape).then(tap).padding(horizontal = 2.dp, vertical = 10.dp),
        Arrangement.spacedBy(2.dp), Alignment.CenterHorizontally
    ) {
        Text(tile.big, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1)
        if (tile.name.isNotBlank()) Text(tile.name, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, maxLines = 2, color = colors.onSurface)
        if (tile.ipa.isNotBlank()) Text(tile.ipa, fontSize = 10.sp, color = colors.primary, textAlign = TextAlign.Center, maxLines = 1)
    }
}

/** Точки-индикатор страниц: текущая вытянута в «таблетку» цвета акцента. */
@Composable
fun PageDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(modifier, Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), Alignment.CenterVertically) {
        repeat(count) { i ->
            val width by animateDpAsState(if (i == current) 22.dp else 8.dp, label = "dot")
            Box(Modifier.height(8.dp).width(width).background(if (i == current) colors.primary else colors.outline, CircleShape))
        }
    }
}
