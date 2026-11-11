package com.dlstudio.app.ui.components
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AccentPalette
import com.dlstudio.app.ui.theme.LocalStrings
@Composable
fun AccentWheel(selected: AccentColor, onSelect: (AccentColor) -> Unit) {
    val strings = LocalStrings.current
    val current = AccentPalette.forAccent(selected)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            AccentPalette.values().forEach { palette ->
                val active = palette == current
                Surface(Modifier.weight(1f).clickable(role = Role.RadioButton) { onSelect(palette.colors[current.slot(selected)]) }, RoundedCornerShape(12.dp), if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background, border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)) {
                    Box(Modifier.padding(vertical = 8.dp), Alignment.Center) {
                        Text(strings.accentPaletteName(palette), style = MaterialTheme.typography.labelMedium, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly, Alignment.Top) {
            current.colors.forEach { accent ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(48.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(accent.main), Color(accent.deep)))).then(if (accent == selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier).clickable(role = Role.RadioButton) { onSelect(accent) }.semantics { this.selected = accent == selected })
                    if (accent == selected) Text(strings.accentName(accent), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
