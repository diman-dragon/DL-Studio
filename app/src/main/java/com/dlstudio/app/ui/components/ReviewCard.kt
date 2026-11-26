package com.dlstudio.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dlstudio.app.progress.ReviewItem
import com.dlstudio.app.ui.theme.LocalStrings

private const val SHOWN = 3

/** «Повторение»: темы, где остались ошибки или пропущенные звуковые вопросы; нажатие открывает практику темы, где показаны только они. */
@Composable
fun ReviewCard(items: List<ReviewItem>, onPractice: (String) -> Unit) {
    val s = LocalStrings.current
    SettingCard(gap = 6.dp) {
        Text(s.reviewTitle.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        items.take(SHOWN).forEach { item ->
            Row(
                Modifier.fillMaxWidth().clickable(role = Role.Button) { onPractice(item.topic.id) }.padding(vertical = 6.dp),
                Arrangement.spacedBy(10.dp), Alignment.CenterVertically
            ) {
                Text(item.topic.title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(s.reviewCounts(item.wrong, item.sound), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (items.size > SHOWN) Text(s.reviewMore(items.size - SHOWN), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
