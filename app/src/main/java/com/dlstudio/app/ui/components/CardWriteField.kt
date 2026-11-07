package com.dlstudio.app.ui.components

import android.os.LocaleList
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged

/** Поле ввода с системным переключением раскладки клавиатуры через imeHintLocales. */
@Composable
fun CardWriteField(value: String, onValueChange: (String) -> Unit, placeholder: String, isRussian: Boolean, enabled: Boolean, borderColor: Color, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme; val shape = RoundedCornerShape(16.dp)
    Box(modifier.height(54.dp).clip(shape).background(cs.surface).border(1.5.dp, borderColor, shape).padding(horizontal = 14.dp), Alignment.CenterStart) {
        AndroidView(modifier = Modifier.fillMaxWidth(), factory = { ctx: android.content.Context ->
            EditText(ctx).apply {
                background = null; isSingleLine = true; imeOptions = EditorInfo.IME_ACTION_DONE
                imeHintLocales = LocaleList.forLanguageTags(if (isRussian) "ru" else "en")
                setTextColor(cs.onSurface.toArgb()); setHintTextColor(cs.onSurfaceVariant.toArgb()); textSize = 16f
                doAfterTextChanged { if (it != null && it.toString() != value) onValueChange(it.toString()) }
                setOnEditorActionListener { _, a, _ -> if (a == EditorInfo.IME_ACTION_DONE) { onDone(); true } else false }
            }
        }, update = { view: EditText ->
            view.isEnabled = enabled; view.hint = placeholder
            view.imeHintLocales = LocaleList.forLanguageTags(if (isRussian) "ru" else "en")
            if (view.text.toString() != value) view.setText(value)
        })
    }
}
