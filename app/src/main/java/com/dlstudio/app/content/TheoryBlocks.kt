package com.dlstudio.app.content

import com.dlstudio.app.speech.Gender
import com.dlstudio.app.speech.Role

/** Вид выноски: совет, заметка, предупреждение, интересный факт. */
enum class CalloutKind(val mark: String) { TIP("!tip "), NOTE("!note "), WARN("!warn "), FUN("!fun ") }

/** Вид плитки-буквы: гласная, согласная или «и та и другая». */
enum class TileKind(val mark: Char) { VOWEL('v'), CONSONANT('c'), BOTH('b') }

/** Плитка: крупный знак, подпись, транскрипция и то, что произносит озвучка (если пусто — сам знак). */
data class Tile(val kind: TileKind, val big: String, val name: String, val ipa: String, val say: String)

/** Блок страницы теории, уже разобранный из абзацев. */
sealed interface Block {
    data class Text(val text: String) : Block
    data class Sub(val text: String) : Block
    data class Bullets(val items: List<String>) : Block
    data class Callout(val kind: CalloutKind, val text: String) : Block
    data class Say(val text: String, val note: String, val say: String, val role: Role = Role()) : Block
    data class Table(val rows: List<List<String>>) : Block
    data class Tiles(val tiles: List<Tile>) : Block
    data class Media(val kind: MediaKind, val src: String, val caption: String) : Block
}

/**
 * Разбор абзацев страницы теории в блоки. Абзац без отметки — обычный текст (в нём **жирный** и _курсив_). Отметки в начале абзаца:
 * - `## ` — подзаголовок;
 * - `- ` — пункт списка (подряд идущие собираются в один список);
 * - `!tip `, `!note `, `!warn `, `!fun ` — цветная выноска;
 * - `~ English | перевод | что озвучить` — фраза с кнопкой озвучки и переводом под ней; третье поле необязательно (если пусто, озвучивается сама фраза);
 *   отметка роли для реплик диалога: `~f ` — женский голос, `~m ` — мужской (`~f2 `, `~m2 ` — вторая женщина или второй мужчина, до `~f4`), `~1 `…`~4 ` — другой голос
 *   без указания пола. Мужская реплика всегда звучит мужским голосом, женская — женским. Две-четыре фразы подряд без отметки сами получают разные голоса;
 * - `| a | b | c` — строка таблицы (подряд идущие — одна таблица, первая строка — шапка);
 * - `@image путь | подпись`, `@audio …`, `@video …` — картинка, звук или видео (путь относительно папки медиа темы или адрес https);
 * - `::v A a | эй | /eɪ/ | ay` — плитка (v — гласная, c — согласная, b — обе); последнее поле (что озвучить) необязательно; подряд идущие — сетка.
 */
object TheoryBlocks {
    private const val UNSET = -1
    private const val MAX_VOICES = 4
    private val MARK = Regex("^~([fm]?)([1-4]?) ")

    fun parse(paragraphs: List<String>): List<Block> {
        val out = ArrayList<Block>()
        for (raw in paragraphs) {
            val p = raw.trim()
            if (p.isEmpty()) continue
            val last = out.lastOrNull()
            when {
                p.startsWith("## ") -> out.add(Block.Sub(p.drop(3).trim()))
                p.startsWith("- ") -> merge(out, last, Block.Bullets(listOf(p.drop(2).trim())))
                p.startsWith("~") && MARK.containsMatchIn(p) -> MARK.find(p)?.let { out.add(say(p.drop(it.value.length), role(it))) }
                p.startsWith("|") -> merge(out, last, Block.Table(listOf(cells(p))))
                p.startsWith("@") -> out.add(media(p) ?: Block.Text(p))
                p.startsWith("::") -> tile(p)?.let { merge(out, last, Block.Tiles(listOf(it))) } ?: out.add(Block.Text(p))
                else -> out.add(callout(p) ?: Block.Text(p))
            }
        }
        return voices(out)
    }

    /** Роль из отметки: `~ ` — не задана (UNSET), `~f `, `~m ` — первый голос пола, цифра — номер голоса. */
    private fun role(m: MatchResult): Role {
        val gender = when (m.groupValues[1]) { "f" -> Gender.F; "m" -> Gender.M; else -> Gender.ANY }
        val digit = m.groupValues[2]
        return Role(gender, if (digit.isNotEmpty()) digit.toInt() - 1 else if (gender == Gender.ANY) UNSET else 0)
    }

    /** Какие полы озвучивают реплики в абзацах (для проверки, хватает ли голосов): подмножество {Gender.F, Gender.M}. */
    fun genders(paragraphs: List<String>): Set<Int> =
        paragraphs.mapNotNull { MARK.find(it.trim())?.let(::role)?.gender }.filter { it != Gender.ANY }.toSet()

    /** Две-четыре фразы подряд без явной роли звучат разными голосами; длинные ряды (слова списком) — одним. */
    private fun voices(blocks: List<Block>): List<Block> {
        val out = ArrayList<Block>(blocks)
        var i = 0
        while (i < out.size) {
            if (out[i] !is Block.Say) { i++; continue }
            var j = i
            while (j < out.size && out[j] is Block.Say) j++
            for (k in i until j) {
                val b = out[k] as Block.Say
                if (b.role.n == UNSET) out[k] = b.copy(role = Role(Gender.ANY, if (j - i in 2..MAX_VOICES) k - i else 0))
            }
            i = j
        }
        return out
    }

    /** Подряд идущие однотипные блоки (список, таблица, сетка) склеиваются в один. */
    private fun merge(out: MutableList<Block>, last: Block?, next: Block) {
        val joined = when {
            last is Block.Bullets && next is Block.Bullets -> Block.Bullets(last.items + next.items)
            last is Block.Table && next is Block.Table -> Block.Table(last.rows + next.rows)
            last is Block.Tiles && next is Block.Tiles -> Block.Tiles(last.tiles + next.tiles)
            else -> null
        }
        if (joined == null) out.add(next) else out[out.lastIndex] = joined
    }

    private fun callout(p: String): Block.Callout? =
        CalloutKind.values().firstOrNull { p.startsWith(it.mark) }?.let { Block.Callout(it, p.drop(it.mark.length).trim()) }

    private fun say(body: String, role: Role): Block.Say {
        val parts = body.split(" | ", limit = 3).map { it.trim() }
        return Block.Say(parts[0], parts.getOrElse(1) { "" }, parts.getOrElse(2) { "" }, role)
    }

    private fun media(p: String): Block.Media? {
        val kind = MediaKind.values().firstOrNull { p.startsWith(it.mark) } ?: return null
        val parts = p.drop(kind.mark.length).split(" | ", limit = 2).map { it.trim() }
        return parts[0].takeIf { it.isNotEmpty() }?.let { Block.Media(kind, it, parts.getOrElse(1) { "" }) }
    }

    private fun cells(row: String): List<String> = row.trim().trim('|').split('|').map { it.trim() }

    private fun tile(p: String): Tile? {
        val kind = TileKind.values().firstOrNull { it.mark == p.getOrNull(2) } ?: return null
        val f = p.drop(3).split('|').map { it.trim() }
        if (f[0].isEmpty()) return null
        return Tile(kind, f[0], f.getOrElse(1) { "" }, f.getOrElse(2) { "" }, f.getOrElse(3) { "" })
    }
}
