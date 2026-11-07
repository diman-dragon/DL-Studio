package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 4 варианта ответа в виде сетки 2x2 под карточкой с использованием компонента ChoiceButton. */
@Composable
fun ChoiceOptions(options: List<String>, correct: String, selected: String?, onPick: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { opt ->
                    ChoiceButton(
                        text = opt,
                        onClick = { onPick(opt) },
                        modifier = Modifier.weight(1f),
                        isChosen = selected != null && opt == selected,
                        isTarget = selected != null && opt == correct,
                        enabled = selected == null
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
