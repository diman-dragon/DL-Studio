package com.dlstudio.app

import android.app.Application
import com.dlstudio.app.data.CrashLog

/** Приложение: сразу включает запись падений, чтобы при следующем запуске показать, что случилось. */
class DlApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashLog.install(this)
    }
}
