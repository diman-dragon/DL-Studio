package com.lingvadesk.app.platform.icon

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.lingvadesk.app.data.local.LingvaDatabase
import java.util.Calendar

/** Уровень тревоги значка: 0 — спокойно, с 12:00 — 1, с 17:00 — 2, с 20:00 — 3 (бордовый). */
object IconLevel {
    fun of(hour: Int, studiedToday: Boolean, hasCards: Boolean): Int =
        if (studiedToday || !hasCards) 0 else if (hour >= 20) 3 else if (hour >= 17) 2 else if (hour >= 12) 1 else 0
}

object IconApply {
    private const val ALIAS_PREFIX = "com.example.lingocards.IconL"

    /** Включает значок уровня `level`, остальные выключает (сначала включаем — чтобы значок не пропадал). */
    fun run(ctx: Context, level: Int) {
        val pm = ctx.packageManager
        for (pass in 0..1) for (i in 0..3) {
            if ((i == level) != (pass == 0)) continue
            val state = if (i == level) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            pm.setComponentEnabledSetting(ComponentName(ctx.packageName, ALIAS_PREFIX + i), state, PackageManager.DONT_KILL_APP)
        }
    }
}

object IconQuery {
    fun level(ctx: Context): Int = try {
        val db = LingvaDatabase.get(ctx).db()
        val now = Calendar.getInstance()
        val day = (now.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        fun count(sql: String) = db.rawQuery(sql, null).use { it.moveToFirst() && it.getLong(0) > 0 }
        IconLevel.of(
            now.get(Calendar.HOUR_OF_DAY),
            count("SELECT COUNT(*) FROM reviews WHERE ts >= ${day.timeInMillis}"),
            count("SELECT COUNT(*) FROM cards WHERE due <= ${now.timeInMillis}")
        )
    } catch (e: Exception) {
        0
    }
}

object IconSchedule {
    fun run(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(ctx, 0, Intent(ctx, IconReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + 60_000, 30 * 60_000L, pi)
    }
}

class IconReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) IconSchedule.run(ctx)
        IconApply.run(ctx, IconQuery.level(ctx))
    }
}
