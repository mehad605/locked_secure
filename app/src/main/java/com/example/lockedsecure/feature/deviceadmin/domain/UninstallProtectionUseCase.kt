package com.example.lockedsecure.feature.deviceadmin.domain

/**
 * Enforces uninstall protection when the application is enabled as a Device Administrator.
 */
interface UninstallProtectionUseCase {
    /**
     * Activates uninstall blocking if the app is configured as Device Owner.
     */
    fun activateIfDeviceOwner(): UninstallProtectionResult
}

/**
 * Default implementation of [UninstallProtectionUseCase].
 */
internal class DefaultUninstallProtectionUseCase(
    private val gateway: DevicePolicyGateway,
) : UninstallProtectionUseCase {
    override fun activateIfDeviceOwner(): UninstallProtectionResult {
        if (!gateway.isDeviceOwner()) {
            return UninstallProtectionResult.NotDeviceOwner
        }
        return gateway.blockAppUninstall().fold(
            onSuccess = { UninstallProtectionResult.Activated },
            onFailure = { error -> UninstallProtectionResult.Failed(error.message) },
        )
    }
}

/**
 * Factory creating the default [UninstallProtectionUseCase] while keeping implementations internal.
 */
object DeviceAdminFactory {
    /**
     * Creates an [UninstallProtectionUseCase] backed by [gateway].
     */
    fun createUseCase(gateway: DevicePolicyGateway): UninstallProtectionUseCase =
        DefaultUninstallProtectionUseCase(gateway)
}
