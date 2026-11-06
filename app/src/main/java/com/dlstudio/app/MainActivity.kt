package com.dlstudio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dlstudio.app.library.LibraryViewModel
import com.dlstudio.app.ui.AppRoot
import com.dlstudio.app.ui.AppViewModel

class MainActivity : ComponentActivity() {
    private val library get() = ViewModelProvider(this)[LibraryViewModel::class.java]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AppViewModel = viewModel()
            val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
                if (uri != null) vm.pickFolder(uri, { library.release() }, { library.reopen() })
            }
            AppRoot(vm) { picker.launch(null) }
        }
    }

    override fun onStart() { super.onStart(); library.refresh() }

    /** Уходим в фон — дописываем базу в папку хранения: ради этого момента выгрузка и откладывалась. */
    override fun onStop() { library.flush(); super.onStop() }
}
