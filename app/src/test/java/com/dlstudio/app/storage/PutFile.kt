package com.dlstudio.app.storage

import java.io.File

/** Создаёт файл по относительному пути (с папками) и пишет в него текст. */
fun File.putFile(path: String, text: String): File = File(this, path).also {
    it.parentFile.mkdirs()
    it.writeText(text)
}
