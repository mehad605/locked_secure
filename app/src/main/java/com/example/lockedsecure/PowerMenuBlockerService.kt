package com.example.lockedsecure

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class PowerMenuBlockerService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // ponytail: check only state changes which are triggered by dialog/window openings
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            val className = event.className?.toString()
            
            // Log for debugging
            Log.d("PowerMenuBlocker", "Window changed: pkg=$packageName class=$className")

            // ponytail: identify power menu dialog window classes across standard and custom ROMs (Samsung, Pixel, Xiaomi, etc.)
            val isPowerMenu = className?.contains("GlobalActions", ignoreCase = true) == true
                    || className?.contains("globalactions", ignoreCase = true) == true
                    || (packageName == "com.android.systemui" && className?.contains("dialog", ignoreCase = true) == true && className.contains("global", ignoreCase = true))

            if (isPowerMenu) {
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                if (keyguardManager.isKeyguardLocked) {
                    // Block the shutdown by dismissing the global actions menu
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    Log.i("PowerMenuBlocker", "Power menu blocked and dismissed on lock screen.")
                } else {
                    Log.d("PowerMenuBlocker", "Power menu opened while screen is unlocked. Allowed.")
                }
            }
        }
    }

    override fun onInterrupt() {
        // No-op
    }
}
