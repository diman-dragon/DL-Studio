package com.dlstudio.app.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/** Имя выбранной пользователем папки; null — папка по умолчанию. */
fun folderName(context: Context, prefs: Prefs): String? =
    prefs.storageUri?.let { DocumentFile.fromTreeUri(context, Uri.parse(it))?.name }
