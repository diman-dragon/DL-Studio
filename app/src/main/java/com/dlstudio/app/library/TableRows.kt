package com.dlstudio.app.library

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
