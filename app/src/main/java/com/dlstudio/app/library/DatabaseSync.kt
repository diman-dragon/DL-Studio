package com.dlstudio.app.library

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.dlstudio.app.storage.StorageNode
import java.io.File
import java.io.IOException

/**
 * Рабочая копия базы лежит во внутренней памяти приложения, а в выбранную папку (SAF) она выгружается
 * отложенно. Маркер `.dirty` помнит, что выгрузка не состоялась, и при следующем запуске побеждает рабочая копия.
 * Битую базу никогда не затираем пустой: при двух негодных копиях открытие завершается ошибкой.
 */
class DatabaseSync(context: Context, private val parent: StorageNode) {
    private val local = File(context.filesDir, "library-${parent.id.hashCode()}.db")
    private val marker = File(local.path + ".dirty")
    private val remote: StorageNode? by lazy { databaseFile(parent, true) }

    fun markDirty() { marker.createNewFile() }

    fun prepare(): File {
        val saved = local.exists() && isValid(local)
        if (saved && marker.exists()) return local
        val stored = databaseFile(parent, false)?.takeIf { it.size > 0 }
        if (stored != null) {
            val tmp = File(local.path + ".tmp")
            stored.openInput().use { i -> tmp.outputStream().use { i.copyTo(it) } }
            if (isValid(tmp)) { local.delete(); if (!tmp.renameTo(local)) throw IOException("Не удалось заменить рабочую копию"); return local }
            tmp.delete()
            if (!saved) throw IOException("База в папке повреждена, рабочей копии нет")
        } else if (local.exists() && !saved) { throw IOException("Рабочая копия базы повреждена") }
        if (saved) markDirty()
        return local
    }

    /** Выгружает рабочую копию в папку хранения; без изменений ничего не делает. */
    fun push() {
        if (!marker.exists()) return
        remote?.openOutput()?.use { out -> local.inputStream().use { it.copyTo(out) } } ?: throw IOException("Нет файла базы")
        marker.delete()
    }

    private fun isValid(file: File): Boolean = runCatching {
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            db.rawQuery("PRAGMA quick_check", null).use { it.moveToFirst() && it.getString(0) == "ok" }
        }
    }.getOrDefault(false)
}
