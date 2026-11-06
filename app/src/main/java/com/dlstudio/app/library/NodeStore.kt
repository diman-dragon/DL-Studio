package com.dlstudio.app.library

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

/** Дерево библиотеки в таблице `nodes`: чтение одним запросом, правки точечные (без стирания всего дерева). */
class NodeStore(private val db: SQLiteDatabase) {
    private class Row(val id: String, val parent: String?, val node: LibNode)

    fun load(): List<LibNode> {
        val rows = db.rawQuery("SELECT id,parent_id,name,back,hint,explanation,example FROM nodes ORDER BY rowid", null).use { c ->
            buildList { while (c.moveToNext()) add(Row(c.getString(0), c.getString(1), LibNode(c.getString(0), c.str(2), c.str(3), c.str(4), c.str(5), c.str(6)))) }
        }
        val byParent = rows.groupBy { it.parent }
        fun build(parent: String?): List<LibNode> = byParent[parent].orEmpty().map { it.node.copy(children = build(it.id)) }
        return build(null)
    }

    private fun android.database.Cursor.str(i: Int) = getString(i) ?: ""

    fun insert(parentId: String?, level: Int, nodes: List<LibNode>) {
        nodes.forEach { n ->
            db.insertOrThrow("nodes", null, ContentValues().apply {
                put("id", n.id); put("parent_id", parentId); put("type", level); put("name", n.name)
                put("back", n.back); put("hint", n.hint); put("explanation", n.explanation); put("example", n.example)
            })
            insert(n.id, level + 1, n.children)
        }
    }

    fun update(n: LibNode) {
        db.update("nodes", ContentValues().apply { put("name", n.name); put("back", n.back); put("hint", n.hint); put("explanation", n.explanation); put("example", n.example) }, "id=?", arrayOf(n.id))
    }

    /** Удаляет узел со всем содержимым; возвращает id удалённых карточек, чтобы стереть их расписание. */
    fun delete(id: String): List<String> {
        val all = mutableListOf(id); var i = 0
        while (i < all.size) { db.rawQuery("SELECT id FROM nodes WHERE parent_id=?", arrayOf(all[i++])).use { c -> while (c.moveToNext()) all += c.getString(0) } }
        val cards = all.filter { db.rawQuery("SELECT 1 FROM nodes WHERE id=? AND type=2", arrayOf(it)).use { c -> c.moveToFirst() } }
        all.forEach { db.delete("nodes", "id=?", arrayOf(it)) }
        return cards
    }

    fun cardCount(): Int = db.rawQuery("SELECT COUNT(*) FROM nodes WHERE type=2", null).use { it.moveToFirst(); it.getInt(0) }
}
