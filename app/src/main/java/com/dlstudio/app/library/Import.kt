package com.dlstudio.app.library

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

/**
 * Дерево из JSON: массив узлов (или один узел) `{name, back, items:[…]}`; id не нужны. Допустимы синонимы:
 * `front` вместо `name`, `translation` вместо `back`, `decks`/`cards` вместо `items`.
 * У карточки необязательны `ipa` (синонимы `transcription`, `phonetic`) и `meanings: [{translation, explanation, example, exampleTranslation}]`;
 * без `back` главным переводом становится перевод первого значения. Узлы без названия отбрасываются.
 */
object ImportJson {
    fun decode(text: String): List<LibNode> = when (val root = JSONTokener(text).nextValue()) {
        is JSONArray -> list(root)
        is JSONObject -> listOf(node(root)).filter { it.name.isNotBlank() }
        else -> emptyList()
    }

    private fun list(array: JSONArray?): List<LibNode> {
        if (array == null) return emptyList()
        return (0 until array.length()).mapNotNull { array.optJSONObject(it) }.map { node(it) }.filter { it.name.isNotBlank() }
    }

    private fun node(o: JSONObject): LibNode {
        val meanings = MeaningsJson.read(o.optJSONArray("meanings"))
        return LibNode(
            "",
            o.optString("name").ifBlank { o.optString("front") },
            o.optString("back").ifBlank { o.optString("translation") }.ifBlank { meanings.firstOrNull()?.translation.orEmpty() },
            o.optString("hint").ifBlank { o.optString("clue") },
            o.optString("explanation").ifBlank { o.optString("definition") },
            o.optString("example").ifBlank { o.optString("exampleSentence") },
            list(o.optJSONArray("items") ?: o.optJSONArray("decks") ?: o.optJSONArray("cards")),
            o.optString("ipa").ifBlank { o.optString("transcription") }.ifBlank { o.optString("phonetic") },
            meanings
        )
    }
}

/** Текст файла → проекты с колодами и карточками (id свежие). JSON — дерево библиотеки, остальное — таблица. */
object ImportParser {
    fun parse(text: String, name: String): List<LibNode> {
        val first = text.trimStart().take(1)
        val json = if (first == "[" || first == "{") runCatching { ImportJson.decode(text) }.getOrNull().orEmpty() else emptyList()
        return wrap(json.ifEmpty { ImportTable.decks(text, name) }, name).withNewIds()
    }

    /** Достраивает дерево до проектов: одни карточки → проект и колода с именем файла, одни колоды → проект. */
    private fun wrap(nodes: List<LibNode>, name: String): List<LibNode> = when (depth(nodes)) {
        0 -> emptyList()
        1 -> listOf(LibNode("", name, children = listOf(LibNode("", name, children = nodes))))
        2 -> listOf(LibNode("", name, children = nodes))
        else -> nodes
    }

    private fun depth(nodes: List<LibNode>): Int = if (nodes.isEmpty()) 0 else 1 + depth(nodes.flatMap { it.children })
}

/** Таблица «слово; перевод» → колоды с карточками. Первая колонка — лицевая сторона, вторая — обратная. */
object ImportTable {
    private val FRONT = setOf("front", "word", "term", "слово", "лицевая", "лицевая сторона")
    private val BACK = setOf("back", "translation", "definition", "перевод", "обратная", "обратная сторона")
    private val DECK = setOf("deck", "колода")
    private val IPA = setOf("ipa", "transcription", "транскрипция")

    /**
     * Строка-заголовок (все её ячейки — известные названия колонок) задаёт порядок колонок; необязательная колонка «ipa» — транскрипция; колонка «deck» делит
     * карточки на колоды, без неё всё идёт в одну колоду `name`. Строки без второй колонки пропускаются.
     */
    fun decks(text: String, name: String): List<LibNode> {
        val all = TableRows.read(text)
        val head = all.firstOrNull().orEmpty().map { it.lowercase() }
        val header = head.isNotEmpty() && head.all { it in FRONT || it in BACK || it in DECK || it in IPA }
        val front = if (header) column(head, FRONT, 0) else 0
        val back = if (header) column(head, BACK, 1) else 1
        val deck = if (header) column(head, DECK, -1) else -1
        val ipa = if (header) column(head, IPA, -1) else -1
        val rows = (if (header) all.drop(1) else all).filter { it.size > 1 && it.at(front).isNotBlank() }
        return rows.groupBy { it.at(deck).ifBlank { name } }.map { (title, group) ->
            LibNode("", title, children = group.map { LibNode("", it.at(front), it.at(back), ipa = it.at(ipa)) })
        }
    }

    private fun column(head: List<String>, names: Set<String>, default: Int): Int =
        head.indexOfFirst { it in names }.takeIf { it >= 0 } ?: default

    private fun List<String>.at(index: Int): String = getOrElse(index) { "" }.trim()
}

/** Итог импорта для показа пользователю: сколько карточек добавлено (0 — в файле ничего не нашлось). */
data class ImportReport(val cards: Int)

/** Строки таблицы из текста: разделитель выбирается по файлу, кавычки CSV учитываются. */
object TableRows {
    /** От надёжных к двусмысленным: запятая бывает внутри перевода, поэтому последняя. */
    private val DELIMITERS = listOf("\t", ";", "|", " — ", " – ", " - ", " = ", ",")

    /** Строки без пустых и служебных `#…` (так начинается экспорт Anki), разрезанные на ячейки; нет разделителя — пусто. */
    fun read(text: String): List<List<String>> {
        val lines = text.lines().filter { it.isNotBlank() && !it.startsWith("#") }
        val delimiter = DELIMITERS.firstOrNull { d -> lines.count { it.contains(d) } * 2 >= lines.size } ?: return emptyList()
        return lines.map { split(it, delimiter) }
    }

    private fun split(line: String, delimiter: String): List<String> =
        if (delimiter.length > 1) line.split(delimiter, limit = 2).map { it.trim() } else splitQuoted(line, delimiter[0])

    private fun splitQuoted(line: String, delimiter: Char): List<String> {
        val cells = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"' && quoted && line.getOrNull(i + 1) == '"') {
                cell.append('"')
                i++
            } else if (c == '"') {
                quoted = !quoted
            } else if (c == delimiter && !quoted) {
                cells.add(cell.toString().trim())
                cell.clear()
            } else {
                cell.append(c)
            }
            i++
        }
        cells.add(cell.toString().trim())
        return cells
    }
}

/** Читает файл и подгоняет под открытый уровень: 0 — проекты, 1 — колоды, 2 — карточки (блокирующая операция). */
fun readImport(context: Context, uri: Uri, level: Int): List<LibNode> {
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: throw IOException("Файл не открыт")
    if (bytes.size > 10_000_000) throw IOException("Файл больше 10 МБ")
    val name = DocumentFile.fromSingleUri(context, uri)?.name.orEmpty().substringBeforeLast('.').ifBlank { "Import" }
    return ImportParser.parse(decodeText(bytes), name).descend(level)
}

/** Импорт без исключений: узлы для вставки на уровень `level` и отчёт; ошибка чтения — пусто и отчёт «0 карточек». */
fun tryImport(context: Context, uri: Uri, level: Int): Pair<List<LibNode>, ImportReport> {
    val nodes = runCatching { readImport(context, uri, level) }.getOrDefault(emptyList())
    return nodes to ImportReport(nodes.descend(2 - level).size)
}

/** Текст файла: UTF-16 (с BOM) или UTF-8 (BOM убирается); не UTF-8 — значит Excel в Windows, то есть windows-1251. */
fun decodeText(bytes: ByteArray): String {
    val head = bytes.take(2).map { it.toInt() and 0xFF }
    if (head == listOf(0xFF, 0xFE) || head == listOf(0xFE, 0xFF)) return String(bytes, Charsets.UTF_16)
    val strict = Charsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)
    val text = runCatching { strict.decode(ByteBuffer.wrap(bytes)).toString() }
        .getOrElse { String(bytes, charset("windows-1251")) }
    return text.removePrefix("\uFEFF")
}

/** Значения слова ⇄ JSON: в базе лежат одной строкой в колонке `meanings`, в файле импорта — массивом `meanings`. Пустые значения отбрасываются. */
object MeaningsJson {
    fun read(array: JSONArray?): List<Meaning> {
        if (array == null) return emptyList()
        return (0 until array.length()).mapNotNull { array.optJSONObject(it) }.map(::meaning).filter { it != Meaning() }
    }

    fun decode(text: String): List<Meaning> = if (text.isBlank()) emptyList() else runCatching { read(JSONArray(text)) }.getOrDefault(emptyList())

    fun encode(meanings: List<Meaning>): String = if (meanings.isEmpty()) "" else JSONArray(meanings.map(::json)).toString()

    private fun meaning(o: JSONObject) = Meaning(
        o.optString("translation").trim(), o.optString("explanation").ifBlank { o.optString("definition") }.trim(),
        o.optString("example").trim(), o.optString("exampleTranslation").ifBlank { o.optString("example_translation") }.trim()
    )

    private fun json(m: Meaning) = JSONObject().put("translation", m.translation).put("explanation", m.explanation).put("example", m.example).put("exampleTranslation", m.exampleTranslation)
}
