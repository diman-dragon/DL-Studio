package com.dlstudio.app.data

import android.content.Context
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Запись последнего падения: перехватчик необработанных ошибок сохраняет отчёт (синхронно, пока процесс не умер) и передаёт ошибку системе как обычно.
 * При следующем запуске приложение показывает отчёт ([Prefs.crashReport]) и очищает его.
 */
object CrashLog {
    fun install(app: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                val version = app.packageManager.getPackageInfo(app.packageName, 0).versionName.orEmpty()
                val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                val report = CrashReport.build(time, version, "${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}", thread.name, error)
                Prefs(app).saveCrashNow(report)
            }
            previous?.uncaughtException(thread, error)
        }
    }
}
