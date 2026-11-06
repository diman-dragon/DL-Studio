package com.dlstudio.app.study

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dlstudio.app.data.Prefs
import java.util.Calendar

/** Ежедневный будильник напоминания; пересоздаётся при запуске, смене настроек и после перезагрузки. */
object StudyReminderScheduler {
    private const val REQUEST = 4107

    fun sync(context: Context, prefs: Prefs) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        val pending = PendingIntent.getBroadcast(context, REQUEST, Intent(context, StudyReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.cancel(pending)
        if (!prefs.remindersEnabled) return
        val time = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, prefs.reminderHour); set(Calendar.MINUTE, prefs.reminderMinute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, time.timeInMillis, AlarmManager.INTERVAL_DAY, pending)
    }
}
