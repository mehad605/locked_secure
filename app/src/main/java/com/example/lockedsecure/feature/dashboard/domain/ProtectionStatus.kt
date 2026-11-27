package com.example.lockedsecure.feature.dashboard.domain

/**
 * Snapshot of the device's anti-theft protection configuration displayed on the dashboard.
 *
 * @property isAccessibilityEnabled True when the power-menu blocker Accessibility Service is active.
 * @property isAdminEnabled True when the Device Administrator receiver is active.
 */
data class ProtectionStatus(
    val isAccessibilityEnabled: Boolean,
    val isAdminEnabled: Boolean,
) {
    /** True only when both power-menu blocking and uninstall protection are enabled. */
    val isFullyProtected: Boolean
        get() = isAccessibilityEnabled && isAdminEnabled
}
