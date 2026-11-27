package com.example.lockedsecure.feature.deviceadmin.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pure JVM unit tests for [UninstallProtectionUseCase] covering all result states.
 */
class UninstallProtectionUseCaseTest {

    @Test
    fun returnsNotDeviceOwnerWithoutCallingBlockUninstall() {
        var blockCalls = 0
        val useCase = DeviceAdminFactory.createUseCase(
            object : DevicePolicyGateway {
                override fun isDeviceOwner(): Boolean = false
                override fun blockAppUninstall(): Result<Unit> {
                    blockCalls++
                    return Result.success(Unit)
                }
            },
        )

        assertEquals(UninstallProtectionResult.NotDeviceOwner, useCase.activateIfDeviceOwner())
        assertEquals(0, blockCalls)
    }

    @Test
    fun returnsActivatedWhenDeviceOwnerSucceeds() {
        val useCase = DeviceAdminFactory.createUseCase(
            object : DevicePolicyGateway {
                override fun isDeviceOwner(): Boolean = true
                override fun blockAppUninstall(): Result<Unit> = Result.success(Unit)
            },
        )

        assertEquals(UninstallProtectionResult.Activated, useCase.activateIfDeviceOwner())
    }

    @Test
    fun returnsFailedWithReasonWhenPlatformThrows() {
        val useCase = DeviceAdminFactory.createUseCase(
            object : DevicePolicyGateway {
                override fun isDeviceOwner(): Boolean = true
                override fun blockAppUninstall(): Result<Unit> =
                    Result.failure(SecurityException("not authorized"))
            },
        )

        assertEquals(
            UninstallProtectionResult.Failed("not authorized"),
            useCase.activateIfDeviceOwner(),
        )
    }
}
