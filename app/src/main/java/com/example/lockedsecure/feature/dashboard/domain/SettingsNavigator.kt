package com.example.lockedsecure.feature.dashboard.domain

/**
 * System settings destinations reachable from the dashboard.
 */
enum class SettingsTarget {
    /** Android Accessibility settings where the power-menu blocker service is toggled. */
    ACCESSIBILITY,

    /** Android Device Administrator enrollment prompt for uninstall protection. */
    DEVICE_ADMIN,
}

/**
 * Typed result returned when attempting to open a system settings screen.
 */
sealed interface NavigationResult {

    /** Target settings activity was started successfully. */
    data object Opened : NavigationResult

    /**
     * No system activity could handle the requested settings intent.
     *
     * @property target Which settings destination failed to launch.
     */
    data class Unavailable(val target: SettingsTarget) : NavigationResult
}

/**
 * Navigates the user to Android system settings screens needed to enable protection.
 */
interface SettingsNavigator {

    /**
     * Launches the system Accessibility settings screen.
     */
    fun openAccessibilitySettings(): NavigationResult

    /**
     * Launches the Device Administrator activation screen.
     */
    fun openDeviceAdminSettings(): NavigationResult
}
