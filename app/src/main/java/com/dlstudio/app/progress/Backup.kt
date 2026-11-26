package com.dlstudio.app.progress

import org.json.JSONObject

/** Резервная копия: прогресс (с этапами, XP, статистикой) и места в темах — один JSON-файл, который можно перенести на другой телефон. */
data class Backup(val progress: Progress, val places: Map<String, Place>) {
    companion object {
        private const val APP = "dlstudio-progress"
        private const val VERSION = 1

        fun encode(progress: Progress, places: Map<String, Place>): String =
            JSONObject().put("app", APP).put("v", VERSION).put("progress", ProgressJson.encode(progress)).put("places", PlaceJson.encode(places)).toString()

        /** Разбирает файл; `null`, если это не копия прогресса DL Studio. */
        fun decode(text: String?): Backup? = runCatching {
            val o = JSONObject(text!!)
            if (o.getString("app") != APP || o.getInt("v") > VERSION) return null
            Backup(ProgressJson.decode(o.getString("progress")), PlaceJson.decode(o.optString("places")))
        }.getOrNull()
    }
}
