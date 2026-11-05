package com.dlstudio.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.dlstudio.app.storage.moveStorage
import java.io.IOException

/** Блокирующая операция (вызывать вне главного потока): переносит DL-Studio в выбранную папку и запоминает её. */
fun applyFolder(context: Context, prefs: Prefs, tree: Uri) {
    val rw = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    context.contentResolver.takePersistableUriPermission(tree, rw)
    val picked = DocumentFile.fromTreeUri(context, tree) ?: throw IOException("Папка недоступна")
    moveStorage(openParent(context, prefs), DocumentNode(context, picked))
    prefs.storageUri?.takeIf { it != tree.toString() }?.let {
        runCatching { context.contentResolver.releasePersistableUriPermission(Uri.parse(it), rw) }
    }
    prefs.storageUri = tree.toString()
}
