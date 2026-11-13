package com.dlstudio.app.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.dlstudio.app.settings.FontChoice
import com.dlstudio.app.settings.FontGroup
import com.dlstudio.app.settings.FontPreset

/**
 * Единые правила набора для любого стиля: наш шрифт и вес; перенос только по границам слов,
 * а длинные слова в обычном тексте — по правилам языка (Hyphens.Auto; в заголовках отключено). Заголовки балансируются (Heading).
 * `scale` — масштаб шрифта группы «Интерфейс» из настроек. Межстрочный интервал — `fontSize × ratio` (заголовки и кнопки плотнее, абзацы чуть свободнее); лишнее место сверху и снизу строки срезано,
 * поэтому текст в кнопках стоит ровно по центру.
 */
fun TextStyle.dlStyle(weight: FontWeight, family: FontFamily = FontFamily.Default, heading: Boolean = false, ratio: Float = if (heading) 1.2f else 1.32f, scale: Float = 1f) = copy(
    fontFamily = family,
    fontSize = (fontSize.value * scale).sp,
    fontWeight = weight,
    hyphens = if (heading) Hyphens.None else Hyphens.Auto,
    lineBreak = if (heading) LineBreak.Heading else LineBreak.Paragraph,
    lineHeight = (fontSize.value * scale * ratio).sp,
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both)
)

/** Типографика интерфейса: шрифт и масштаб группы «Интерфейс» из настроек; везде безопасные переносы. */
fun dlTypography(family: FontFamily, scale: Float): Typography = Typography().let { b ->
    Typography(
        displayLarge = b.displayLarge.dlStyle(FontWeight.Bold, family, heading = true, scale = scale),
        displayMedium = b.displayMedium.dlStyle(FontWeight.Bold, family, heading = true, scale = scale),
        displaySmall = b.displaySmall.dlStyle(FontWeight.Bold, family, heading = true, scale = scale),
        headlineLarge = b.headlineLarge.dlStyle(FontWeight.Bold, family, heading = true, scale = scale),
        headlineMedium = b.headlineMedium.dlStyle(FontWeight.Bold, family, heading = true, scale = scale),
        headlineSmall = b.headlineSmall.dlStyle(FontWeight.SemiBold, family, heading = true, scale = scale),
        titleLarge = b.titleLarge.dlStyle(FontWeight.SemiBold, family, heading = true, scale = scale),
        titleMedium = b.titleMedium.dlStyle(FontWeight.SemiBold, family, heading = true, scale = scale),
        titleSmall = b.titleSmall.dlStyle(FontWeight.SemiBold, family, heading = true, scale = scale),
        bodyLarge = b.bodyLarge.dlStyle(FontWeight.Normal, family, scale = scale),
        bodyMedium = b.bodyMedium.dlStyle(FontWeight.Normal, family, scale = scale),
        bodySmall = b.bodySmall.dlStyle(FontWeight.Normal, family, scale = scale),
        labelLarge = b.labelLarge.dlStyle(FontWeight.Medium, family, ratio = 1.2f, scale = scale),
        labelMedium = b.labelMedium.dlStyle(FontWeight.Medium, family, ratio = 1.2f, scale = scale),
        labelSmall = b.labelSmall.dlStyle(FontWeight.Medium, family, ratio = 1.2f, scale = scale)
    )
}

/** Семейство Compose для выбранного шрифта. */
fun FontChoice.family(): FontFamily = when (this) {
    FontChoice.SANS -> FontFamily.Default
    FontChoice.SERIF -> FontFamily.Serif
    FontChoice.MONO -> FontFamily.Monospace
    FontChoice.CURSIVE -> FontFamily.Cursive
}

/** Тот же шрифт для обычных Android-виджетов (поле ввода). */
fun FontChoice.face(): Typeface = when (this) {
    FontChoice.SANS -> Typeface.DEFAULT
    FontChoice.SERIF -> Typeface.SERIF
    FontChoice.MONO -> Typeface.MONOSPACE
    FontChoice.CURSIVE -> Typeface.create("cursive", Typeface.NORMAL)
}

/** Применяет к стилю шрифт и размер выбранной группы из настроек (группа «Интерфейс» уже учтена в теме). */
@Composable
fun TextStyle.inFont(group: FontGroup): TextStyle {
    if (group == FontGroup.UI) return this
    val spec = LocalFonts.current[group]
    val k = spec.scale / 100f
    return copy(
        fontFamily = spec.choice.family(),
        fontSize = if (fontSize.isSp) (fontSize.value * k).sp else fontSize,
        lineHeight = if (lineHeight.isSp) (lineHeight.value * k).sp else lineHeight
    )
}

private const val WORD = 40f
private const val TEXT = 16f
private const val BUTTON_SIZE = 14f

/**
 * Все «ручные» размеры текста в одном месте: группа настроек, наибольший и наименьший кегль (sp) для адаптивного режима,
 * `fixed` — единственный размер режима «Системный» (их всего три: слово, текст, кнопки), вес и межстрочный коэффициент.
 * Компоненты берут размер отсюда (`style()`), а не пишут числа в месте вызова.
 */
enum class TextRole(val group: FontGroup, val max: Float, val min: Float, val fixed: Float, val weight: FontWeight, val ratio: Float = 1.15f) {
    CARD_WORD(FontGroup.CARD, 72f, 18f, WORD, FontWeight.SemiBold),
    CARD_MINI(FontGroup.CARD, 28f, 18f, TEXT, FontWeight.SemiBold),
    CARD_INPUT(FontGroup.CARD, 16f, 16f, TEXT, FontWeight.Normal, 1.3f),
    IPA(FontGroup.IPA, 26f, 14f, TEXT, FontWeight.SemiBold, 1.2f),
    NOTE(FontGroup.NOTE, 24f, 13f, TEXT, FontWeight.Normal, 1.25f),
    FEEDBACK(FontGroup.NOTE, 16f, 12f, TEXT, FontWeight.Normal, 1.25f),
    BUTTON(FontGroup.BUTTON, 44f, 14f, BUTTON_SIZE, FontWeight.SemiBold),
    BUTTON_BIG(FontGroup.BUTTON, 36f, 14f, BUTTON_SIZE, FontWeight.SemiBold),
    BUTTON_CHIP(FontGroup.BUTTON, 20f, 10f, BUTTON_SIZE, FontWeight.SemiBold),
    EMOJI(FontGroup.UI, 28f, 28f, 28f, FontWeight.Normal)
}

/** Стиль роли: вес из `TextRole`, размер — наибольший (адаптивный режим) или фиксированный (системный), шрифт и масштаб — из настроек её группы. */
@Composable
fun TextRole.style(): TextStyle {
    val size = if (LocalFonts.current.preset == FontPreset.SYSTEM) fixed else max
    return MaterialTheme.typography.bodyLarge.copy(fontSize = size.sp, lineHeight = (size * ratio).sp, fontWeight = weight, letterSpacing = 0.sp).inFont(group)
}
