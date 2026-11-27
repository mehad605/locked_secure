package com.example.lockedsecure.feature.deviceadmin.domain

/**
 * Outcome of attempting to activate Device Owner uninstall blocking.
 */
sealed interface UninstallProtectionResult {
    /** Device Owner mode was active and uninstall blocking was enabled. */
    data object Activated : UninstallProtectionResult

    /** App is a standard Device Admin (not Device Owner); no additional DPM call needed. */
    data object NotDeviceOwner : UninstallProtectionResult

    /**
     * Device Owner API threw an error while blocking uninstallation.
     *
     * @property reason Human-readable diagnostic message from the platform failure.
     */
    data class Failed(
        val reason: String?,
    ) : UninstallProtectionResult
}
