package com.dlstudio.app.study

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

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
