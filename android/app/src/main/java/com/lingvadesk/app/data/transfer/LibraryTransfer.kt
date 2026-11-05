package com.lingvadesk.app.data.transfer

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.lingvadesk.app.data.local.LingvaDatabase
import com.lingvadesk.app.domain.model.ImportResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/** Импорт/экспорт библиотеки: `lingvadesk-library` v1 и `flashcards-deck` v1 (docs/FORMATS.md). Импорт сливает, не дублирует. */
@Singleton
class LibraryTransfer @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val database: LingvaDatabase
) {
    suspend fun export(uri: Uri): Int = withContext(Dispatchers.IO) {
        val db = database.db()
        val projects = JSONArray()
        var cards = 0
        db.rawQuery("SELECT id, name, tts FROM languages ORDER BY name COLLATE NOCASE", null).use { pc ->
            while (pc.moveToNext()) {
                val decks = JSONArray()
                db.rawQuery("SELECT id, name FROM decks WHERE language_id = ? ORDER BY id", arrayOf(pc.getString(0))).use { dc ->
                    while (dc.moveToNext()) {
                        val arr = JSONArray()
                        db.rawQuery("SELECT front, back, example, example_tr, cloze, cloze_answer, cloze_tr FROM cards WHERE deck_id = ? ORDER BY ord, id", arrayOf(dc.getString(0))).use { cc ->
                            while (cc.moveToNext()) {
                                arr.put(JSONObject().apply {
                                    put("front", cc.getString(0)); put("back", cc.getString(1))
                                    cc.getString(2)?.let { put("example", it) }
                                    cc.getString(3)?.let { put("example_translation", it) }
                                    cc.getString(4)?.let { put("cloze", it) }
                                    cc.getString(5)?.let { put("cloze_answer", it) }
                                    cc.getString(6)?.let { put("cloze_translation", it) }
                                })
                                cards++
                            }
                        }
                        decks.put(JSONObject().put("name", dc.getString(1)).put("cards", arr))
                    }
                }
                projects.put(JSONObject().put("name", pc.getString(1)).put("tts", pc.getString(2)).put("decks", decks))
            }
        }
        val json = JSONObject().put("format", "lingvadesk-library").put("version", 1).put("projects", projects)
        ctx.contentResolver.openOutputStream(uri, "wt")?.use { it.write(json.toString(2).toByteArray(Charsets.UTF_8)) }
            ?: throw IllegalStateException("Не удалось открыть файл для записи")
        cards
    }

    suspend fun import(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        val text = ctx.contentResolver.openInputStream(uri)?.use { String(it.readBytes(), Charsets.UTF_8) }
            ?: throw IllegalStateException("Не удалось открыть файл")
        save(parse(text))
    }

    /** Приводит оба формата к списку проектов. */
    internal fun parse(text: String): JSONArray {
        val root = try { JSONObject(text.trim().removePrefix("﻿")) } catch (e: Exception) { throw IllegalArgumentException("Файл не похож на библиотеку LingvaDesk") }
        return when (root.optString("format")) {
            "lingvadesk-library" -> root.optJSONArray("projects") ?: JSONArray()
            "flashcards-deck" -> {
                val lang = root.optJSONObject("language") ?: JSONObject()
                val deck = JSONObject().put("name", root.optJSONObject("deck")?.optString("name").orEmpty()).put("cards", root.optJSONArray("cards") ?: JSONArray())
                JSONArray().put(JSONObject().put("name", lang.optString("name")).put("tts", lang.optString("tts", "en-US")).put("decks", JSONArray().put(deck)))
            }
            else -> throw IllegalArgumentException("Файл не похож на библиотеку LingvaDesk")
        }
    }

    private fun JSONObject.text(vararg keys: String): String? =
        keys.firstNotNullOfOrNull { k -> optString(k, "").trim().takeIf { it.isNotEmpty() } }

    private fun save(projects: JSONArray): ImportResult {
        val db = database.db()
        var p = 0; var d = 0; var added = 0; var skipped = 0
        db.beginTransaction()
        try {
            for (i in 0 until projects.length()) {
                val pj = projects.optJSONObject(i) ?: continue
                val pname = pj.optString("name").trim().ifEmpty { continue }
                var pid = db.rawQuery("SELECT id FROM languages WHERE name = ?", arrayOf(pname)).use { if (it.moveToFirst()) it.getLong(0) else -1L }
                if (pid < 0) {
                    pid = db.insertOrThrow("languages", null, ContentValues().apply { put("name", pname); put("tts", pj.optString("tts", "en-US").ifBlank { "en-US" }) }); p++
                }
                val decks = pj.optJSONArray("decks") ?: continue
                for (j in 0 until decks.length()) {
                    val dj = decks.optJSONObject(j) ?: continue
                    val dname = dj.optString("name").trim().ifEmpty { continue }
                    var did = db.rawQuery("SELECT id FROM decks WHERE language_id = ? AND name = ?", arrayOf(pid.toString(), dname)).use { if (it.moveToFirst()) it.getLong(0) else -1L }
                    if (did < 0) {
                        did = db.insertOrThrow("decks", null, ContentValues().apply { put("language_id", pid); put("name", dname); put("created", System.currentTimeMillis()) }); d++
                    }
                    var ord = db.rawQuery("SELECT COALESCE(MAX(ord), -1) + 1 FROM cards WHERE deck_id = ?", arrayOf(did.toString())).use { it.moveToFirst(); it.getInt(0) }
                    val cards = dj.optJSONArray("cards") ?: continue
                    for (k in 0 until cards.length()) {
                        val cj = cards.optJSONObject(k) ?: continue
                        val front = cj.text("front") ?: continue
                        val back = cj.text("back") ?: continue
                        val exists = db.rawQuery("SELECT 1 FROM cards WHERE deck_id = ? AND front = ? AND back = ? LIMIT 1", arrayOf(did.toString(), front, back)).use { it.moveToFirst() }
                        if (exists) { skipped++; continue }
                        db.insertOrThrow("cards", null, ContentValues().apply {
                            put("deck_id", did); put("front", front); put("back", back)
                            put("example", cj.text("example")); put("example_tr", cj.text("example_translation", "example_tr"))
                            put("cloze", cj.text("cloze")); put("cloze_answer", cj.text("cloze_answer"))
                            put("cloze_tr", cj.text("cloze_translation", "cloze_tr")); put("ord", ord++)
                        })
                        added++
                    }
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return ImportResult(p, d, added, skipped)
    }
}
