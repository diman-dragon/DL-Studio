package com.dlstudio.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dlstudio.app.data.Prefs
import com.dlstudio.app.data.folderName
import com.dlstudio.app.data.moveToFolder
import com.dlstudio.app.data.openParent
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.settings.cycleNext
import com.dlstudio.app.storage.ensureLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(private val app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    private val mutable = MutableStateFlow(AppState(prefs.theme, prefs.lang, folderName(app, prefs)))
    val state: StateFlow<AppState> = mutable.asStateFlow()

    init {
        // структура DL-Studio существует с первого запуска
        viewModelScope.launch(Dispatchers.IO) { runCatching { ensureLayout(openParent(app, prefs)) } }
    }

    fun cycleTheme() {
        prefs.theme = prefs.theme.cycleNext()
        mutable.update { it.copy(theme = prefs.theme) }
    }

    fun cycleLang() {
        prefs.lang = prefs.lang.cycleNext()
        mutable.update { it.copy(lang = prefs.lang) }
    }

    fun pickFolder(tree: Uri) {
        if (mutable.value.status == StorageStatus.MOVING) return
        mutable.update { it.copy(status = StorageStatus.MOVING) }
        viewModelScope.launch {
            val status = moveToFolder(app, prefs, tree)
            mutable.update { it.copy(folderName = folderName(app, prefs), status = status) }
        }
    }
}
