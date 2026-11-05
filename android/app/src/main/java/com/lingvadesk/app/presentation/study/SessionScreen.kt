package com.lingvadesk.app.presentation.study

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognizerIntent
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingvadesk.app.domain.study.PrefKeys
import com.lingvadesk.app.domain.study.QuizTask
import com.lingvadesk.app.domain.study.TaskKind
import com.lingvadesk.app.presentation.common.components.CardFaceContainer
import com.lingvadesk.app.presentation.common.components.LingvaButton
import com.lingvadesk.app.presentation.common.components.LingvaTextField
import com.lingvadesk.app.presentation.common.components.ScreenTitle
import kotlinx.coroutines.delay

private const val SHOW_TEXT_AFTER_MS = 6_000L

private fun speechAvailable(ctx: Context) =
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).resolveActivity(ctx.packageManager) != null

private fun vibrate(ctx: Context, enabled: Boolean) {
    if (!enabled) return
    val v = ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)) else @Suppress("DEPRECATION") v.vibrate(30)
}

@Composable
fun SessionScreen(scope: String, settings: Map<String, String>, onExit: () -> Unit, vm: SessionViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val ui by vm.ui.collectAsStateWithLifecycle()
    val rate = settings[PrefKeys.SPEECH_RATE]?.toFloatOrNull() ?: 1f
    val vibrateOn = settings[PrefKeys.VIBRATE] != "0"

    LaunchedEffect(scope) { vm.start(scope, speechAvailable(ctx)) }
    BackHandler { vm.stopAudio(); onExit() }

    // экран не гаснет во время учёбы
    DisposableEffect(settings[PrefKeys.KEEP_AWAKE]) {
        val window = (ctx as? Activity)?.window
        if (settings[PrefKeys.KEEP_AWAKE] != "0") window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    // «отвлёкся»: время в фоне не считается
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_PAUSE) vm.onPause() else if (e == Lifecycle.Event.ON_RESUME) vm.onResume()
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    val recognizer = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) {
            val data = r.data
            vm.onSpeech(
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).orEmpty(),
                data?.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES)
            )
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenTitle(
            title = if (ui.retry) "Повтор ошибок" else "Учёба",
            onBack = { vm.stopAudio(); onExit() },
            actions = { if (ui.canUndo) TextButton(onClick = vm::undoLast) { Text("↶") } }
        )
        when {
            ui.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            ui.error != null -> Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) { Text(ui.error ?: "", color = MaterialTheme.colorScheme.error) }
            ui.finished -> Finished(ui, onRetry = vm::retryMistakes, onExit = onExit)
            else -> {
                val sc = ui.current
                val task = ui.task
                if (sc != null && task != null) {
                    Text(
                        if (ui.left == 0) "✓ План выполнен" else "Осталось ${ui.left} из ${ui.total}",
                        Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodyMedium
                    )
                    Column(
                        Modifier.weight(1f).fillMaxWidth().padding(16.dp)
                            .pointerInput(sc.card.id) {
                                var dx = 0f
                                detectHorizontalDragGestures(
                                    onDragStart = { dx = 0f },
                                    onHorizontalDrag = { _, d -> dx += d },
                                    onDragEnd = { if (dx < -80f) vm.next() else if (dx > 80f) vm.prev() }
                                )
                            },
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Task(ui, task, sc.tts, rate, vm, onSpeak = {
                            vm.stopAudio()
                            recognizer.launch(
                                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                                    .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    .putExtra(RecognizerIntent.EXTRA_LANGUAGE, sc.tts)
                                    .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                            )
                        })
                    }
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = vm::prev, Modifier.weight(1f)) { Text("‹ Назад") }
                        Button(onClick = { vibrate(ctx, vibrateOn); vm.next() }, Modifier.weight(1f)) { Text("Далее ›") }
                    }
                }
            }
        }
    }
}

@Composable
private fun Finished(ui: SessionUi, onRetry: () -> Unit, onExit: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(if (ui.total == 0) "На сегодня карточек нет" else "✓ План выполнен", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        if (ui.mistakes > 0) {
            LingvaButton("Повторить ошибки (${ui.mistakes})", onRetry)
            Spacer(Modifier.height(8.dp))
        }
        LingvaButton("Готово", onExit)
    }
}

@Composable
private fun Task(ui: SessionUi, task: QuizTask, lang: String, rate: Float, vm: SessionViewModel, onSpeak: () -> Unit) {
    val card = ui.current!!.card
    val answered = ui.correct != null
    var typed by remember(card.id) { mutableStateOf("") }

    // «Показать текстом» в задании на слух появляется через 6 с (место занято сразу — вёрстка не прыгает)
    var canShowText by remember(card.id) { mutableStateOf(false) }
    LaunchedEffect(card.id) { delay(SHOW_TEXT_AFTER_MS); canShowText = true }

    when (task.kind) {
        TaskKind.FLIP -> {
            CardFaceContainer(task.prompt, task.answer, ui.flipped, onFlip = vm::flip)
            if (ui.flipped && !card.example.isNullOrBlank()) {
                Text(card.example, Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                if (!card.exampleTr.isNullOrBlank()) Text(card.exampleTr, Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Speaker(lang, rate, vm)
        }
        TaskKind.CHOICE -> {
            QuestionCard(task.prompt)
            task.choices.forEach { c ->
                val color = when {
                    !answered -> ButtonDefaults.outlinedButtonColors()
                    c == task.answer -> ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    c == ui.picked -> ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    else -> ButtonDefaults.outlinedButtonColors()
                }
                OutlinedButton(onClick = { vm.answer(c) }, Modifier.fillMaxWidth(), enabled = !answered, colors = color) { Text(c) }
            }
            Speaker(lang, rate, vm)
        }
        TaskKind.TYPE, TaskKind.CLOZE, TaskKind.LISTEN -> {
            when (task.kind) {
                TaskKind.LISTEN -> {
                    Text("Что вы слышите?", style = MaterialTheme.typography.titleMedium)
                    Speaker(lang, rate, vm)
                    if (ui.textShown) QuestionCard(task.answer)
                    else TextButton(onClick = vm::showText, enabled = canShowText) { Text("Показать текстом") }
                }
                TaskKind.CLOZE -> {
                    QuestionCard(task.prompt)
                    if (!card.clozeTr.isNullOrBlank()) Text(card.clozeTr, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> QuestionCard(task.prompt)
            }
            LingvaTextField(typed, { typed = it }, "Ответ", enabled = !answered)
            if (!answered) LingvaButton("Проверить", { vm.answer(typed) }, enabled = typed.isNotBlank())
        }
        TaskKind.SPEAK -> {
            QuestionCard(task.prompt)
            LingvaButton(if (ui.heard == null) "🎤 Скажите вслух" else "🎤 Ещё раз", onSpeak, enabled = !answered)
            ui.heard?.let { Text("Услышано: «$it» · ${(ui.score ?: 0.0).times(100).toInt()} %") }
            TextButton(onClick = vm::showText) { Text("Подсказка") }
            if (ui.textShown) QuestionCard(task.answer)
        }
    }
    if (answered) {
        Text(
            if (ui.correct == true) "✓ Верно" else "✗ Правильно: ${task.answer}",
            color = if (ui.correct == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun QuestionCard(text: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
            Text(text, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun Speaker(lang: String, rate: Float, vm: SessionViewModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        FilledTonalButton(onClick = { vm.sayText(lang, rate) }) { Text("🔊") }
        OutlinedButton(onClick = { vm.saySlow(800, lang) }) { Text("🐢") }
        OutlinedButton(onClick = { vm.saySlow(1800, lang) }) { Text("🐌") }
    }
}
