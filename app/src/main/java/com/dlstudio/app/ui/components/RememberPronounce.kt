package com.dlstudio.app.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.dlstudio.app.study.Listener

/** Состояние проверки произношения для `target`; разрешение на микрофон спрашивается при первом нажатии, запись снимается при уходе с карточки. */
@Composable
fun rememberPronounce(target: String, tag: String): PronounceState {
    val context = LocalContext.current
    val state = remember(target, tag) {
        PronounceState(Listener(context), target, tag) { ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) state.start() else state.deny() }
    state.askPermission = { launcher.launch(Manifest.permission.RECORD_AUDIO) }
    DisposableEffect(state) { onDispose { state.release() } }
    return state
}
