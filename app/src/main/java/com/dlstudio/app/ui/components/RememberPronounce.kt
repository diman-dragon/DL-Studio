package com.dlstudio.app.ui.components

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.dlstudio.app.study.Listener
import com.dlstudio.app.study.RecognitionIntent

/** Состояние проверки произношения для `target`; микрофон спрашивается при первом нажатии, запись снимается при уходе с карточки, запасной путь — системное окно ввода. */
@Composable
fun rememberPronounce(target: String, tag: String): PronounceState {
    val context = LocalContext.current
    val state = remember(target, tag) {
        PronounceState(Listener(context), target, tag) { ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) state.start() else state.deny() }
    val dialog = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        state.dialogResult(if (r.resultCode == Activity.RESULT_OK) r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) else null)
    }
    state.askPermission = { permission.launch(Manifest.permission.RECORD_AUDIO) }
    state.openDialog = { try { dialog.launch(RecognitionIntent.build(context, tag, false)); true } catch (e: ActivityNotFoundException) { false } }
    DisposableEffect(state) { onDispose { state.release() } }
    return state
}
