package com.dlstudio.app.study

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dlstudio.app.MainActivity
import com.dlstudio.app.R
import com.dlstudio.app.data.Prefs
import com.dlstudio.app.i18n.stringsFor
import java.util.Calendar

/** Переключает обычную и тревожную иконку launcher при появлении просроченных карточек. */
object LauncherIcon {
    private const val DEFAULT = "com.dlstudio.app.LauncherDefault"
    private const val ALERT = "com.dlstudio.app.LauncherAlert"

    fun sync(context: Context, hasOverdue: Boolean) {
        val manager = context.packageManager
        set(manager, ComponentName(context, DEFAULT), !hasOverdue)
        set(manager, ComponentName(context, ALERT), hasOverdue)
    }

    private fun set(manager: PackageManager, component: ComponentName, enabled: Boolean) {
        manager.setComponentEnabledSetting(
            component,
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}

/** После перезагрузки телефона будильники сбрасываются — ставим напоминание заново. */
class StudyBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) StudyReminderScheduler.sync(context, Prefs(context))
    }
}

/** Срабатывает по будильнику: уведомляет, только если есть что повторять; текст — на языке приложения. */
class StudyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = Prefs(context)
        val today = DayMath.dayNumber(System.currentTimeMillis())
        val snapshot = ReminderSnapshot.decode(prefs.reminderSnapshot)
        if (!prefs.remindersEnabled || snapshot == null || !snapshot.shouldNotify(today)) return
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val s = stringsFor(prefs.lang)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL, s.reminderChannel, NotificationManager.IMPORTANCE_DEFAULT))
        }
        val open = PendingIntent.getActivity(context, 4108, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val due = snapshot.dueCount(today)
        val text = if (due > 0) s.reminderDue(due) else s.reminderText
        manager.notify(4107, NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_tab_study_fill).setNumber(due).setContentTitle("DL Studio").setContentText(text).setContentIntent(open).setAutoCancel(true).build())
    }

    private companion object { const val CHANNEL = "study" }
}

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
