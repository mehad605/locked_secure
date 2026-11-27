package com.example.lockedsecure.feature.dashboard.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM unit tests for [DashboardController] and [ProtectionStatus].
 */
class DashboardControllerTest {

    @Test
    fun protectionStatusIsFullyProtectedOnlyWhenBothEnabled() {
        assertFalse(ProtectionStatus(isAccessibilityEnabled = false, isAdminEnabled = false).isFullyProtected)
        assertFalse(ProtectionStatus(isAccessibilityEnabled = true, isAdminEnabled = false).isFullyProtected)
        assertFalse(ProtectionStatus(isAccessibilityEnabled = false, isAdminEnabled = true).isFullyProtected)
        assertTrue(ProtectionStatus(isAccessibilityEnabled = true, isAdminEnabled = true).isFullyProtected)
    }

    @Test
    fun controllerDelegatesStatusAndNavigationCalls() {
        val expectedStatus = ProtectionStatus(isAccessibilityEnabled = true, isAdminEnabled = false)
        val navigator = object : SettingsNavigator {
            override fun openAccessibilitySettings(): NavigationResult = NavigationResult.Opened
            override fun openDeviceAdminSettings(): NavigationResult =
                NavigationResult.Unavailable(SettingsTarget.DEVICE_ADMIN)
        }
        val controller = DashboardController(
            statusReader = { expectedStatus },
            navigator = navigator,
        )

        assertEquals(expectedStatus, controller.loadStatus())
        assertEquals(NavigationResult.Opened, controller.onPowerBlockerAction())
        assertEquals(
            NavigationResult.Unavailable(SettingsTarget.DEVICE_ADMIN),
            controller.onAdminProtectionAction(),
        )
    }
}
