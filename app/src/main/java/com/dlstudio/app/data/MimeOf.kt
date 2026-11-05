package com.dlstudio.app.data

import android.webkit.MimeTypeMap

/** MIME по расширению имени; неизвестное — octet-stream (провайдер оставит имя как есть). */
fun mimeOf(name: String): String {
    val extension = name.substringAfterLast('.', "").lowercase()
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
}
