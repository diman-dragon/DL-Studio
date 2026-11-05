package com.lingvadesk.app.presentation.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingvadesk.app.BuildConfig
import com.lingvadesk.app.data.transfer.BackupManager
import com.lingvadesk.app.domain.study.Option
import com.lingvadesk.app.domain.study.PrefKeys
import com.lingvadesk.app.domain.study.PrefOptions
import com.lingvadesk.app.presentation.AppViewModel
import com.lingvadesk.app.presentation.common.components.ConfirmDialog
import com.lingvadesk.app.presentation.common.components.LingvaButton
import com.lingvadesk.app.presentation.common.components.PrefTile
import com.lingvadesk.app.presentation.common.components.ScreenTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val backup: BackupManager) : ViewModel() {
    private val _msg = MutableStateFlow<String?>(null)
    val msg: StateFlow<String?> = _msg.asStateFlow()
    fun consumed() { _msg.value = null }

    fun create(uri: android.net.Uri) = viewModelScope.launch {
        _msg.value = try { backup.create(uri); "Резервная копия создана" } catch (e: Exception) { e.message ?: "Ошибка" }
    }

    fun restore(uri: android.net.Uri, done: () -> Unit) = viewModelScope.launch {
        _msg.value = try { val safety = backup.restore(uri); done(); "Восстановлено. Защитная копия: $safety" } catch (e: Exception) { e.message ?: "Ошибка" }
    }
}

private class Tile(val key: String, val title: String, val options: List<Option>)

private val tiles = listOf(
    Tile(PrefKeys.THEME, "Тема", PrefOptions.theme),
    Tile(PrefKeys.FONT, "Размер шрифта", PrefOptions.font),
    Tile(PrefKeys.LAYOUT, "Вид списков", PrefOptions.layout),
    Tile(PrefKeys.NEW_PER_DAY, "Новых в день (на колоду)", PrefOptions.newPerDay),
    Tile(PrefKeys.SESSION_MAX, "Размер сессии", PrefOptions.sessionMax),
    Tile(PrefKeys.SPEECH_RATE, "Скорость речи", PrefOptions.speechRate),
    Tile(PrefKeys.VIBRATE, "Вибрация", PrefOptions.onOff),
    Tile(PrefKeys.KEEP_AWAKE, "Экран не гаснет в учёбе", PrefOptions.onOff)
)

@Composable
fun SettingsScreen(app: AppViewModel, settings: Map<String, String>, vm: SettingsViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val msg by vm.msg.collectAsState()
    var about by remember { mutableStateOf(false) }
    var pendingRestore by remember { mutableStateOf<android.net.Uri?>(null) }

    LaunchedEffect(msg) { msg?.let { Toast.makeText(ctx, it, Toast.LENGTH_LONG).show(); vm.consumed() } }
    val creator = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { u -> if (u != null) vm.create(u) }
    val opener = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u -> if (u != null) pendingRestore = u }

    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Настройки")
        Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tiles.forEach { t ->
                PrefTile(t.title, PrefOptions.labelOf(t.options, settings[t.key], t.options.first().label)) { app.cycle(t.key, t.options) }
            }
            Spacer(Modifier.height(8.dp))
            LingvaButton("Создать резервную копию", { creator.launch("LingvaDesk-${System.currentTimeMillis()}.fcbak") })
            LingvaButton("Восстановить из копии", { opener.launch(arrayOf("*/*")) })
            LingvaButton("О приложении", { about = true })
        }
    }
    pendingRestore?.let { uri ->
        ConfirmDialog("Восстановить?", "Текущие данные будут заменены (перед этим создаётся защитная копия).", "Восстановить",
            onConfirm = { pendingRestore = null; vm.restore(uri) { app.reload() } }, onDismiss = { pendingRestore = null })
    }
    if (about) AlertDialog(
        onDismissRequest = { about = false }, title = { Text("LingvaDesk") },
        text = { Text("Версия ${BuildConfig.VERSION_NAME}\nОфлайн-карточки: проект → колода → карточка.") },
        confirmButton = { TextButton(onClick = { about = false }) { Text("Закрыть") } }
    )
}
