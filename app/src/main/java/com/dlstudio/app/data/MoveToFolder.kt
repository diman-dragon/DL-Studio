package com.dlstudio.app.data

import android.content.Context
import android.net.Uri
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.storage.InsideStorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Перенос в фоне; результат — статус для показа пользователю. Старые данные при ошибке не страдают. */
suspend fun moveToFolder(context: Context, prefs: Prefs, tree: Uri): StorageStatus =
    withContext(Dispatchers.IO) {
        try {
            applyFolder(context, prefs, tree)
            StorageStatus.MOVED
        } catch (e: InsideStorageException) {
            StorageStatus.ERROR_INSIDE
        } catch (e: Exception) {
            StorageStatus.ERROR_IO
        }
    }
