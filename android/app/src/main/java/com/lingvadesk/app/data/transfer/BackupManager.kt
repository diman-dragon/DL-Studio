package com.lingvadesk.app.data.transfer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.lingvadesk.app.data.local.LingvaDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/** Резервная копия `.fcbak` — ZIP { manifest.json, app.db }; формат совместим с версиями 1.x–2.x. Восстановление: проверка → защитная копия → замена → проверка, при ошибке откат. */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val database: LingvaDatabase
) {
    suspend fun create(uri: Uri) = withContext(Dispatchers.IO) {
        database.db()                                   // файл БД гарантированно существует
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { z ->
            val manifest = JSONObject().put("format", FORMAT).put("version", 1)
                .put("dbVersion", LingvaDatabase.VERSION).put("created", System.currentTimeMillis())
            z.putNextEntry(ZipEntry(MANIFEST)); z.write(manifest.toString(2).toByteArray(Charsets.UTF_8)); z.closeEntry()
            z.putNextEntry(ZipEntry(DB)); z.write(database.file.readBytes()); z.closeEntry()
        }
        ctx.contentResolver.openOutputStream(uri, "wt")?.use { it.write(bytes.toByteArray()) }
            ?: throw IllegalStateException("Не удалось открыть файл для записи")
    }

    /** Возвращает имя защитной копии. */
    suspend fun restore(uri: Uri): String = withContext(Dispatchers.IO) {
        val zip = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: throw IllegalStateException("Не удалось открыть файл")
        var manifest: JSONObject? = null
        var dbBytes: ByteArray? = null
        ZipInputStream(ByteArrayInputStream(zip)).use { z ->
            var e = z.nextEntry
            while (e != null) {
                val data = z.readBytes()
                if (e.name == MANIFEST) manifest = JSONObject(String(data, Charsets.UTF_8)) else if (e.name == DB) dbBytes = data
                e = z.nextEntry
            }
        }
        val m = manifest
        val bytes = dbBytes
        if (m == null || bytes == null || m.optString("format") != FORMAT) throw IllegalArgumentException("Это не файл .fcbak")
        if (m.optInt("dbVersion") > LingvaDatabase.VERSION) throw IllegalArgumentException("Копия создана более новой версией приложения")

        val tmp = File(ctx.cacheDir, "restore.db")
        try {
            tmp.writeBytes(bytes)
            if (!isValid(tmp)) throw IllegalArgumentException("Файл повреждён: проверка БД не пройдена. Текущие данные не тронуты.")
            database.db(); database.close()
            val dir = File(ctx.filesDir, "backups").apply { mkdirs() }
            val safety = File(dir, "pre-restore-${System.currentTimeMillis()}.db")
            database.file.copyTo(safety, overwrite = true)
            try {
                swap(tmp)
            } catch (e: Exception) {
                database.close()
                safety.copyTo(database.file, overwrite = true)
                dropSidecars()
                database.db()
                throw IllegalStateException("Восстановление не удалось, данные возвращены из защитной копии")
            }
            safety.name
        } finally {
            tmp.delete()
        }
    }

    private fun swap(tmp: File) {
        database.close()
        tmp.copyTo(database.file, overwrite = true)
        dropSidecars()
        database.db()                                   // открытие применяет миграции старых копий
        database.close()
        if (!isValid(database.file)) throw java.io.IOException("verify failed")
        database.db()
    }

    private fun dropSidecars() {
        for (s in listOf("-wal", "-shm", "-journal")) File(database.file.path + s).delete()
    }

    private fun isValid(f: File): Boolean {
        var db: SQLiteDatabase? = null
        return try {
            db = SQLiteDatabase.openDatabase(f.path, null, SQLiteDatabase.OPEN_READONLY)
            val ok = db.rawQuery("PRAGMA integrity_check", null).use { it.moveToFirst() && it.getString(0) == "ok" }
            ok && db.rawQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name IN ('languages','decks','cards','settings')", null).use { it.moveToFirst() && it.getInt(0) == 4 }
        } catch (e: Exception) {
            false
        } finally {
            db?.close()
        }
    }

    companion object {
        const val FORMAT = "fcbak"
        const val MANIFEST = "manifest.json"
        const val DB = "app.db"
    }
}
