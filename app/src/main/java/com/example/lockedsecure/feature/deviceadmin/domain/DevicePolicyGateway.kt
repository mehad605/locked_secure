package com.example.lockedsecure.feature.deviceadmin.domain

/**
 * Platform gateway abstracting Android `DevicePolicyManager` uninstall-protection operations.
 */
interface DevicePolicyGateway {
    /**
     * Returns true when this application holds Device Owner privilege.
     */
    fun isDeviceOwner(): Boolean

    /**
     * Requests the platform to block uninstallation of this application.
     *
     * @return [Result.success] when applied, or [Result.failure] with the underlying platform exception.
     */
    fun blockAppUninstall(): Result<Unit>
}
