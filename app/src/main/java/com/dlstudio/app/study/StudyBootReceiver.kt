package com.dlstudio.app.study

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dlstudio.app.data.Prefs

/** После перезагрузки телефона будильники сбрасываются — ставим напоминание заново. */
class StudyBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) StudyReminderScheduler.sync(context, Prefs(context))
    }
}
