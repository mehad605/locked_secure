package com.example.lockedsecure

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.lockedsecure.app.LockedSecureApp
import com.example.lockedsecure.feature.powerblocker.domain.PowerMenuActionPolicy
import com.example.lockedsecure.feature.powerblocker.domain.PowerMenuDecision
import com.example.lockedsecure.feature.powerblocker.domain.PowerMenuEvent

/**
 * Accessibility service entry point that intercepts and dismisses the system power menu
 * while the device keyguard is locked.
 *
 * Kept at `com.example.lockedsecure.PowerMenuBlockerService` to preserve manifest component
 * identity across app upgrades so users do not lose their enabled Accessibility Service state.
 */
class PowerMenuBlockerService : AccessibilityService() {
    private val actionPolicy: PowerMenuActionPolicy by lazy {
        LockedSecureApp.graph(this).powerMenuActionPolicy
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val domainEvent =
            PowerMenuEvent(
                isWindowStateChanged = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                packageName = event.packageName?.toString(),
                className = event.className?.toString(),
            )
        if (actionPolicy.evaluate(domainEvent) == PowerMenuDecision.BLOCK_AND_DISMISS) {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    override fun onInterrupt() {
        // No transient feedback or held resources to release on interrupt.
    }
}
