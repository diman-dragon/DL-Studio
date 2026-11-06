package com.dlstudio.app.study

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dlstudio.app.MainActivity
import com.dlstudio.app.R
import com.dlstudio.app.data.Prefs
import com.dlstudio.app.i18n.stringsFor

/** Срабатывает по будильнику: уведомляет, только если есть что повторять; текст — на языке приложения. */
class StudyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = Prefs(context)
        val today = DayMath.dayNumber(System.currentTimeMillis())
        val snapshot = ReminderSnapshot.decode(prefs.reminderSnapshot)
        if (!prefs.remindersEnabled || snapshot?.shouldNotify(today) == false) return
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        val s = stringsFor(prefs.lang)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL, s.reminderChannel, NotificationManager.IMPORTANCE_DEFAULT))
        }
        val open = PendingIntent.getActivity(context, 4108, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val due = snapshot?.dueCount(today) ?: 0
        val text = if (due > 0) s.reminderDue(due) else s.reminderText
        manager.notify(4107, NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_tab_study_fill).setContentTitle("DL Studio").setContentText(text).setContentIntent(open).setAutoCancel(true).build())
    }

    private companion object { const val CHANNEL = "study" }
}
