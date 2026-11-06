package com.dlstudio.app.library

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.dlstudio.app.storage.StorageNode
import java.io.Closeable

/** База библиотеки: дерево, расписание и журнал. Не потокобезопасна — все вызовы идут под одним замком модели. */
class LibraryDatabase(context: Context, parent: StorageNode) : Closeable {
    private val sync = DatabaseSync(context, parent)
    private val db: SQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(sync.prepare(), null).also(DbSchema::migrate)
    val nodes = NodeStore(db)
    val reviews = ReviewStore(db)

    /** Выполняет правку и помечает базу как не выгруженную в папку хранения. */
    fun <T> write(block: () -> T): T { sync.markDirty(); return block() }

    fun push() = sync.push()

    override fun close() = db.close()
}
