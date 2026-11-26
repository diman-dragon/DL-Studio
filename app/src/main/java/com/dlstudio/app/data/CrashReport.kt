package com.dlstudio.app.data

import java.io.PrintWriter
import java.io.StringWriter

/** Текст отчёта о падении: когда, версия, устройство, поток и полный стек с причинами (обрезается до [LIMIT] знаков). */
object CrashReport {
    const val LIMIT = 6000

    fun build(time: String, version: String, device: String, thread: String, error: Throwable): String {
        val trace = StringWriter().also { error.printStackTrace(PrintWriter(it)) }.toString().trim()
        val head = "DL Studio $version · $device\n$time · поток: $thread\n\n"
        return (head + trace).take(LIMIT)
    }
}
