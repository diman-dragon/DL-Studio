package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.settings.FontGroup
import com.dlstudio.app.study.PronounceCheck
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.TextRole
import com.dlstudio.app.ui.theme.inFont
import com.dlstudio.app.ui.theme.style

/**
 * Текст по центру области: кегль подбирается от размера роли (с учётом настроек) до её минимума так, чтобы всё поместилось.
 * В адаптивном режиме это наибольший размер роли (текст занимает область максимально), в системном — фиксированный (только уменьшается, если не влезает);
 * всё помещается (по ширине и высоте области, не больше `maxLines` строк) и слова не рвались.
 * Нужна ограниченная по ширине область; по высоте — желательно тоже. Внутри `IntrinsicSize` не использовать.
 */
@Composable
fun BigText(text: AnnotatedString, role: TextRole, modifier: Modifier = Modifier, color: Color = Color.Unspecified, maxLines: Int = 2) {
    val measurer = rememberTextMeasurer()
    val style = role.style()
    val maxSize = style.fontSize.value
    val minSize = maxSize * role.min / role.max
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val width = constraints.maxWidth
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else Int.MAX_VALUE
        val size = remember(text, style, width, height, maxLines) {
            TextFit.size(measurer, text, style, width, height, maxSize, minSize, maxLines)
        }
        Text(text, color = color, style = TextFit.styled(style, size).copy(textAlign = TextAlign.Center), maxLines = maxLines, softWrap = true)
    }
}

/**
 * Текст, который сам уменьшается, пока не поместится: ни обрезков, ни разрыва слова посередине.
 * В одну строку слово не переносится вовсе; в несколько — переносы только по правилам языка.
 * Межстрочный интервал уменьшается вместе с кеглем, чтобы пропорция строки не менялась. Ниже половины исходного кегля не уменьшается.
 */
@Composable
fun FitText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    maxLines: Int = 1
) {
    val floor = style.fontSize.value / 2
    var size by remember(text, style, maxLines) { mutableStateOf(style.fontSize) }
    val line = if (style.lineHeight.isSp) (style.lineHeight.value * size.value / style.fontSize.value).sp else style.lineHeight
    Text(
        text, modifier, color, size, lineHeight = line, style = style, maxLines = maxLines, softWrap = maxLines > 1,
        onTextLayout = { r ->
            val cut = TextFit.wordBreak(text, r)
            if ((r.didOverflowWidth || r.didOverflowHeight || cut) && size.value > floor) size = (size.value * 0.92f).coerceAtLeast(floor).sp
        }
    )
}

/** Подбор самого крупного кегля, при котором текст целиком помещается в область, а слова не рвутся посередине. */
object TextFit {
    fun wordBreak(text: String, result: TextLayoutResult): Boolean = (0 until result.lineCount - 1).any { i ->
        val e = result.getLineEnd(i, true)
        e in 1 until text.length && !text[e - 1].isWhitespace() && !text[e].isWhitespace() && text[e - 1] != '-'
    }
    /** Стиль с кеглем `size`; межстрочный интервал сохраняет пропорцию исходного стиля, переносов внутри слова нет. */
    fun styled(style: TextStyle, size: Float): TextStyle {
        val ratio = if (style.lineHeight.isSp && style.fontSize.value > 0f) style.lineHeight.value / style.fontSize.value else 1.2f
        return style.copy(fontSize = size.sp, lineHeight = (size * ratio).sp, hyphens = Hyphens.None)
    }

    /** Наибольший кегль от `min` до `max` (sp) для области `width` × `height` px; если не помещается и минимальный — вернёт `min`. */
    fun size(measurer: TextMeasurer, text: AnnotatedString, style: TextStyle, width: Int, height: Int, max: Float, min: Float, maxLines: Int): Float {
        if (fits(measurer, text, styled(style, max), width, height, maxLines)) return max
        var low = min
        var high = max
        while (high - low > 0.5f) {
            val mid = (low + high) / 2
            if (fits(measurer, text, styled(style, mid), width, height, maxLines)) low = mid else high = mid
        }
        return low
    }

    private fun fits(measurer: TextMeasurer, text: AnnotatedString, style: TextStyle, width: Int, height: Int, maxLines: Int): Boolean {
        val r = measurer.measure(text = text, style = style, maxLines = maxLines, constraints = Constraints(maxWidth = width))
        return !wordBreak(text.text, r) && !r.didOverflowHeight && !r.didOverflowWidth && r.size.height <= height
    }
}

/**
 * Подпись кнопки: слово с заглавной буквы занимает 90% ширины кнопки (по краям небольшой зазор), кегль роли (шрифт и размер — из настроек «Кнопки») подбирается
 * так, чтобы слово было максимально крупным и не рвалось. Высоту области
 * ограничивает вызывающий через `modifier` (например, `fillMaxHeight(0.9f)`).
 */
@Composable
fun ButtonLabel(text: String, color: Color, modifier: Modifier = Modifier, role: TextRole = TextRole.BUTTON, maxLines: Int = 2) {
    BigText(AnnotatedString(text.capFirst()), role, modifier.fillMaxWidth(0.9f), color, maxLines)
}

/**
 * Главное слово карточки: с заглавной буквы, по центру, максимально крупно в выделенной области (размер и шрифт — из роли `role`), слова не рвутся.
 * `fill = false` — текст занимает только свою высоту (в пределах заданной), чтобы под ним можно было поставить транскрипцию.
 * Если передано `heard` (то, что услышано в микрофон), правильно произнесённые слова подсвечиваются зелёным.
 */
@Composable
fun CardWord(text: String, color: Color, modifier: Modifier = Modifier, heard: String = "", fill: Boolean = true, role: TextRole = TextRole.CARD_WORD) {
    val shown = SpokenHighlight.build(text.capFirst(), heard, MaterialTheme.colorScheme.primary)
    BigText(shown, role, modifier.then(if (fill) Modifier.fillMaxSize() else Modifier.fillMaxWidth()), color, maxLines = 6)
}

/** Блок подсказки, пояснения или примера: с заглавной буквы, кегль роли «пояснения» подбирается по месту (по умолчанию до 3 строк; `maxLines` больше — для длинных пояснений), слова не рвутся. */
@Composable
fun CardNote(text: String, modifier: Modifier = Modifier, maxLines: Int = 3) {
    val cs = MaterialTheme.colorScheme
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.surfaceContainerHighest).padding(horizontal = 14.dp, vertical = 8.dp), Alignment.Center) {
        BigText(AnnotatedString(text.capFirst()), TextRole.NOTE, Modifier.fillMaxWidth(), cs.onSurface, maxLines)
    }
}

/** Транскрипция под словом; если она записана без скобок, берётся в косые черты: `kæt` → `/kæt/`. */
@Composable
fun IpaText(ipa: String, modifier: Modifier = Modifier) {
    val shown = if (ipa.first() == '/' || ipa.first() == '[') ipa else "/$ipa/"
    FitText(shown, TextRole.IPA.style().copy(textAlign = TextAlign.Center), modifier, MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun ExpandableNote(text: String, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable(text) { mutableStateOf(false) }
    val strings = LocalStrings.current
    CardNote(text, modifier.clickable(role = Role.Button) { expanded = true })
    if (expanded) AlertDialog(
        onDismissRequest = { expanded = false },
        title = { Text(strings.cardExplanation) },
        text = { GroupText(text, FontGroup.NOTE, Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) },
        confirmButton = { TextButton(onClick = { expanded = false }) { Text(strings.close) } }
    )
}

/** Первая буква заглавная (остальной текст как есть): слова на карточках не начинаются с маленькой буквы. */
fun String.capFirst(): String = replaceFirstChar { it.titlecase() }

/** Обычный текст в шрифте и размере выбранной группы (карточка, транскрипция, пояснения, кнопки). */
@Composable
fun GroupText(text: String, group: FontGroup, modifier: Modifier = Modifier, color: Color = Color.Unspecified, style: TextStyle = MaterialTheme.typography.bodyLarge) {
    Text(text, modifier, color, style = style.inFont(group))
}

/** Эмодзи единого размера: размер задан ролью, а не числом в месте вызова. */
@Composable
fun EmojiText(emoji: String, modifier: Modifier = Modifier) {
    Text(emoji, modifier, fontSize = TextRole.EMOJI.max.sp)
}

/** Строка состояния операции; при `busy` над ней идёт индикатор. */
@Composable
fun StatusText(text: String, busy: Boolean) {
    Column {
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Текст карточки, в котором правильно произнесённые слова (по тому, что услышал распознаватель) выделены цветом. */
object SpokenHighlight {
    private val word = Regex("\\S+")

    fun build(text: String, heard: String, ok: Color): AnnotatedString {
        if (heard.isBlank()) return AnnotatedString(text)
        val matched = PronounceCheck.matchedWords(text, heard)
        return buildAnnotatedString {
            var last = 0
            word.findAll(text).forEachIndexed { i, m ->
                append(text.substring(last, m.range.first))
                if (matched.getOrNull(i) == true) withStyle(SpanStyle(color = ok, fontWeight = FontWeight.Bold)) { append(m.value) } else append(m.value)
                last = m.range.last + 1
            }
            append(text.substring(last))
        }
    }
}

/** Заголовок экрана: одна строка, при нехватке места шрифт уменьшается (слово не рвётся). */
@Composable
fun ScreenHeader(text: String) {
    FitText(text, MaterialTheme.typography.headlineMedium, Modifier.fillMaxWidth().padding(bottom = 16.dp))
}
