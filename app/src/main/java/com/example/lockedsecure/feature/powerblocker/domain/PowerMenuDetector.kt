package com.example.lockedsecure.feature.powerblocker.domain

/**
 * Classifies whether a [PowerMenuEvent] represents a system power or global-actions dialog.
 */
fun interface PowerMenuDetector {
    /**
     * Returns true if [event] matches a system power/shutdown dialog.
     */
    fun isPowerMenu(event: PowerMenuEvent): Boolean
}

/**
 * Default cross-OEM power-menu detector covering AOSP, Pixel, Samsung, Xiaomi, and Realme/ColorOS dialogs.
 *
 * Matches when a window-state change exposes a class name containing `GlobalActions` (case-insensitive)
 * or when `com.android.systemui` opens a dialog whose class name contains both `dialog` and `global`.
 */
internal class DefaultPowerMenuDetector : PowerMenuDetector {
    override fun isPowerMenu(event: PowerMenuEvent): Boolean {
        val className = event.className?.takeIf { event.isWindowStateChanged } ?: return false
        val matchesGlobalActions = className.contains(GLOBAL_ACTIONS_TOKEN, ignoreCase = true)
        val matchesSystemUiDialog =
            event.packageName == SYSTEM_UI_PACKAGE &&
                className.contains(DIALOG_TOKEN, ignoreCase = true) &&
                className.contains(GLOBAL_TOKEN, ignoreCase = true)
        return matchesGlobalActions || matchesSystemUiDialog
    }

    private companion object {
        const val SYSTEM_UI_PACKAGE = "com.android.systemui"
        const val GLOBAL_ACTIONS_TOKEN = "GlobalActions"
        const val DIALOG_TOKEN = "dialog"
        const val GLOBAL_TOKEN = "global"
    }
}
