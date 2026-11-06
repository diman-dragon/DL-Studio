package com.dlstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.study.CardState
import com.dlstudio.app.study.Grade
import com.dlstudio.app.study.Sm2
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.tint
import androidx.compose.foundation.clickable

/** Три кнопки оценки; под каждой — когда карточка вернётся при этом ответе. */
@Composable
fun GradeBar(state: CardState?, today: Long, onGrade: (Grade) -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val names = mapOf(Grade.AGAIN to s.gradeAgain, Grade.HARD to s.gradeHard, Grade.KNOW to s.gradeKnow)
    Row(modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
        Grade.values().forEach { grade ->
            val color = grade.tint()
            Surface(
                Modifier.weight(1f).clickable(role = Role.Button) { onGrade(grade) }, RoundedCornerShape(20.dp),
                color.copy(alpha = 0.16f), border = BorderStroke(1.dp, color.copy(alpha = 0.55f))
            ) {
                Column(Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    FitText(names.getValue(grade), MaterialTheme.typography.titleMedium, color = color, minSize = 11.sp)
                    Text(s.interval(Sm2.daysUntilDue(state, grade, today)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
