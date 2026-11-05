package com.dlstudio.app.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.dlstudio.app.storage.StorageNode

/** Папка, внутри которой лежит DL-Studio: выбранная пользователем (если ещё доступна) или по умолчанию. */
fun openParent(context: Context, prefs: Prefs): StorageNode {
    val tree = prefs.storageUri?.let { DocumentFile.fromTreeUri(context, Uri.parse(it)) }
    return if (tree != null && tree.exists() && tree.canWrite()) DocumentNode(context, tree) else defaultParent(context)
}
