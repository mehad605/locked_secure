package com.example.lockedsecure.feature.dashboard.domain

/**
 * Coordinates dashboard state queries and system-settings navigation actions.
 *
 * Pure Kotlin controller decoupled from Android Activities and View toolkits.
 */
class DashboardController(
    private val statusReader: ProtectionStatusReader,
    private val navigator: SettingsNavigator,
) {

    /**
     * Reads the latest [ProtectionStatus] from the platform.
     */
    fun loadStatus(): ProtectionStatus = statusReader.readStatus()

    /**
     * Requests opening Android Accessibility settings.
     */
    fun onPowerBlockerAction(): NavigationResult = navigator.openAccessibilitySettings()

    /**
     * Requests opening Android Device Administrator settings.
     */
    fun onAdminProtectionAction(): NavigationResult = navigator.openDeviceAdminSettings()
}
