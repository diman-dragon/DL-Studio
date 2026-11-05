package com.lingvadesk.app.presentation.library

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingvadesk.app.domain.model.Card
import com.lingvadesk.app.domain.model.DeckStat
import com.lingvadesk.app.domain.model.ProjectStat
import com.lingvadesk.app.domain.study.PrefKeys
import com.lingvadesk.app.domain.study.PrefOptions
import com.lingvadesk.app.presentation.common.components.*

/** Форма: что сейчас редактируем. */
private sealed interface Form {
    object NewProject : Form
    data class EditProject(val p: ProjectStat) : Form
    object NewDeck : Form
    data class EditDeck(val d: DeckStat) : Form
    object NewCard : Form
    data class EditCard(val c: Card) : Form
}

@Composable
fun LibraryScreen(grid: Boolean, vm: LibraryViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    var form by remember { mutableStateOf<Form?>(null) }
    var confirm by remember { mutableStateOf<(() -> Unit)?>(null) }
    var confirmText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.reload() }
    LaunchedEffect(ui.message) { ui.message?.let { Toast.makeText(ctx, it, Toast.LENGTH_LONG).show(); vm.messageShown() } }
    BackHandler(enabled = ui.level != Level.Projects) { vm.back() }
    BackHandler(enabled = ui.level == Level.Projects) { /* первый экран: «Назад» из приложения не выходит */ }

    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) vm.import(uri) }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> if (uri != null) vm.export(uri) }

    Column(Modifier.fillMaxSize()) {
        val title = when (ui.level) { Level.Projects -> "Библиотека"; Level.Decks -> ui.project?.project?.name ?: "Колоды"; Level.Cards -> ui.deck?.deck?.name ?: "Карточки" }
        ScreenTitle(
            title = title, onBack = if (ui.level != Level.Projects) ({ vm.back() }) else null,
            actions = {
                if (ui.level == Level.Projects) {
                    TextButton(onClick = { importer.launch(arrayOf("application/json", "text/plain", "*/*")) }) { Text("Импорт") }
                    TextButton(onClick = { exporter.launch("lingvadesk-library.json") }) { Text("Экспорт") }
                }
                IconButton(onClick = {
                    form = when (ui.level) { Level.Projects -> Form.NewProject; Level.Decks -> Form.NewDeck; Level.Cards -> Form.NewCard }
                }) { Icon(Icons.Filled.Add, contentDescription = "Добавить") }
            }
        )
        when (ui.level) {
            Level.Projects -> if (ui.projects.isEmpty()) EmptyHint("Создайте проект (язык, тема, даты — что угодно) кнопкой «+» или импортируйте библиотеку.")
            else ItemsView(ui.projects, grid) { p ->
                ItemCard(p.project.name, "${p.decks} кол. · ${p.total} карт. · голос ${p.project.tts}", onClick = { vm.openProject(p) },
                    trailing = { Row2(onEdit = { form = Form.EditProject(p) }, onDelete = {
                        confirmText = "Проект «${p.project.name}» со всеми колодами и карточками будет удалён."; confirm = { vm.deleteProject(p.project.id) }
                    }) })
            }
            Level.Decks -> if (ui.decks.isEmpty()) EmptyHint("В проекте нет колод. Нажмите «+».")
            else ItemsView(ui.decks, grid) { d ->
                ItemCard(d.deck.name, "${d.total} карт. · ${d.due} повторить · ${d.fresh} новых", onClick = { vm.openDeck(d) },
                    trailing = { Row2(onEdit = { form = Form.EditDeck(d) }, onDelete = {
                        confirmText = "Колода «${d.deck.name}» с карточками будет удалена."; confirm = { vm.deleteDeck(d.deck.id) }
                    }) })
            }
            Level.Cards -> if (ui.cards.isEmpty()) EmptyHint("В колоде нет карточек. Нажмите «+».")
            else ItemsView(ui.cards, grid) { c ->
                ItemCard(c.front, c.back, onClick = { form = Form.EditCard(c) },
                    trailing = { TextButton(onClick = { confirmText = "Карточка «${c.front}» будет удалена."; confirm = { vm.deleteCard(c.id) } }) { Text("✕") } })
            }
        }
    }

    when (val f = form) {
        Form.NewProject -> ProjectForm("Новый проект", "", "en-US", { n, t -> vm.addProject(n, t) }) { form = null }
        is Form.EditProject -> ProjectForm("Проект", f.p.project.name, f.p.project.tts, { n, t -> vm.editProject(f.p.project.id, n, t) }) { form = null }
        Form.NewDeck -> FormDialog("Новая колода", listOf("Название" to ""), { vm.addDeck(it[0]); form = null }, { form = null })
        is Form.EditDeck -> DeckForm(f.d, vm) { form = null }
        Form.NewCard -> CardForm("Новая карточка", null, { c -> vm.addCard(c); form = null }) { form = null }
        is Form.EditCard -> CardForm("Карточка", f.c, { c -> vm.editCard(c); form = null }) { form = null }
        null -> {}
    }
    confirm?.let { action -> ConfirmDialog("Удалить?", confirmText, onConfirm = { action(); confirm = null }, onDismiss = { confirm = null }) }
}

@Composable
private fun Row2(onEdit: () -> Unit, onDelete: () -> Unit) {
    androidx.compose.foundation.layout.Row {
        TextButton(onClick = onEdit) { Text("✎") }
        TextButton(onClick = onDelete) { Text("✕") }
    }
}

@Composable
private fun ProjectForm(title: String, name: String, tts: String, onSave: (String, String) -> Unit, onDismiss: () -> Unit) {
    FormDialog(title, listOf("Название" to name, "Голос озвучки (en-US, de-DE…)" to tts), { onSave(it[0], it[1]); onDismiss() }, onDismiss)
}

@Composable
private fun CardForm(title: String, card: Card?, onSave: (Card) -> Unit, onDismiss: () -> Unit) {
    FormDialog(
        title,
        listOf(
            "Слово" to (card?.front ?: ""), "Перевод" to (card?.back ?: ""),
            "Пример" to (card?.example ?: ""), "Перевод примера" to (card?.exampleTr ?: ""),
            "Фраза с пропуском (_)" to (card?.cloze ?: ""), "Пропущенное слово" to (card?.clozeAnswer ?: "")
        ),
        { v ->
            onSave((card ?: Card(0, 0, "", "")).copy(front = v[0], back = v[1], example = v[2], exampleTr = v[3], cloze = v[4], clozeAnswer = v[5]))
        },
        onDismiss
    )
}

/** Форма колоды: название + собственные параметры повторения (плитки по кругу). */
@Composable
private fun DeckForm(d: DeckStat, vm: LibraryViewModel, onDismiss: () -> Unit) {
    val prefs by vm.deckPrefs.collectAsStateWithLifecycle()
    LaunchedEffect(d.deck.id) { vm.loadPrefs(d.deck.id) }
    FormDialog(
        "Колода", listOf("Название" to d.deck.name),
        onSave = { vm.editDeck(d.deck.id, it[0]); onDismiss() }, onDismiss = onDismiss,
        extra = {
            PrefTile("Новых в день", PrefOptions.labelOf(PrefOptions.newPerDay, prefs[PrefKeys.DECK_NEW_PER_DAY], "общие")) { vm.cycleDeckPref(d.deck.id, PrefKeys.DECK_NEW_PER_DAY, PrefOptions.newPerDay) }
            PrefTile("Размер плана", PrefOptions.labelOf(PrefOptions.sessionMax, prefs[PrefKeys.DECK_SESSION_MAX], "общий")) { vm.cycleDeckPref(d.deck.id, PrefKeys.DECK_SESSION_MAX, PrefOptions.sessionMax) }
            PrefTile("Темп интервалов", PrefOptions.labelOf(PrefOptions.pace, prefs[PrefKeys.DECK_PACE] ?: "1")) { vm.cycleDeckPref(d.deck.id, PrefKeys.DECK_PACE, PrefOptions.pace) }
            PrefTile("Порядок новых", PrefOptions.labelOf(PrefOptions.order, prefs[PrefKeys.DECK_ORDER] ?: "shuffle")) { vm.cycleDeckPref(d.deck.id, PrefKeys.DECK_ORDER, PrefOptions.order) }
        }
    )
}
