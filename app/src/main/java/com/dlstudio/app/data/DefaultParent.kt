package com.dlstudio.app.data

import android.content.Context
import com.dlstudio.app.storage.FileNode
import com.dlstudio.app.storage.StorageNode

/** Папка по умолчанию — личная папка приложения: разрешения не нужны, пока пользователь не выбрал свою. */
fun defaultParent(context: Context): StorageNode = FileNode(context.getExternalFilesDir(null) ?: context.filesDir)
