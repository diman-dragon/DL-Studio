package com.dlstudio.app.study

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.dlstudio.app.MainActivity
import com.dlstudio.app.R

class StudyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) { val manager = context.getSystemService(NotificationManager::class.java); manager.createNotificationChannel(NotificationChannel("study", "Учёба", NotificationManager.IMPORTANCE_DEFAULT)); val open = android.app.PendingIntent.getActivity(context, 4108, Intent(context, MainActivity::class.java), android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE); manager.notify(4107, NotificationCompat.Builder(context, "study").setSmallIcon(R.drawable.ic_tab_study_fill).setContentTitle("DL Studio").setContentText("Время повторить карточки").setContentIntent(open).setAutoCancel(true).build()) }
}
