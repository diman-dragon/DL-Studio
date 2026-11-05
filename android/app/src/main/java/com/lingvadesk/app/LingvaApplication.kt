package com.lingvadesk.app

import android.app.Application
import com.lingvadesk.app.platform.icon.IconSchedule
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LingvaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        IconSchedule.run(this)      // значок пересчитывается по базе раз в 30 минут, даже когда приложение не запущено
    }
}
