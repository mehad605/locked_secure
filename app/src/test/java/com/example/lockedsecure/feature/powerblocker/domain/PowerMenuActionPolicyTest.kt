package com.example.lockedsecure.feature.powerblocker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM unit tests for the power-menu blocker domain logic (zero Android framework dependency).
 */
class PowerMenuActionPolicyTest {

    @Test
    fun lazyLockStateIsNotQueriedForIrrelevantWindows() {
        var lockQueries = 0
        val policy = PowerBlockerFactory.createPolicy(
            lockStateProvider = {
                lockQueries++
                true
            },
        )

        val decision = policy.evaluate(
            PowerMenuEvent(
                isWindowStateChanged = true,
                packageName = "com.android.launcher",
                className = "com.android.launcher.Launcher",
            ),
        )

        assertEquals(PowerMenuDecision.ALLOW, decision)
        assertEquals("Lock state should not be queried for non-power-menu events", 0, lockQueries)
    }

    @Test
    fun blocksMatchingPowerMenuOnlyWhenLocked() {
        var locked = true
        val policy = PowerBlockerFactory.createPolicy(lockStateProvider = { locked })
        val event = PowerMenuEvent(
            isWindowStateChanged = true,
            packageName = "com.android.systemui",
            className = "com.android.systemui.globalactions.GlobalActionsDialogLite",
        )

        assertEquals(PowerMenuDecision.BLOCK_AND_DISMISS, policy.evaluate(event))

        locked = false
        assertEquals(PowerMenuDecision.ALLOW, policy.evaluate(event))
    }

    @Test
    fun customDetectorCanBeSubstitutedBehindInterface() {
        val customDetector = PowerMenuDetector { it.className == "CustomShutdownSheet" }
        val policy = PowerBlockerFactory.createPolicy(
            lockStateProvider = { true },
            detector = customDetector,
        )

        assertTrue(
            policy.evaluate(
                PowerMenuEvent(true, "com.oem.ui", "CustomShutdownSheet"),
            ) == PowerMenuDecision.BLOCK_AND_DISMISS,
        )
        assertFalse(
            policy.evaluate(
                PowerMenuEvent(true, "com.android.systemui", "GlobalActionsDialog"),
            ) == PowerMenuDecision.BLOCK_AND_DISMISS,
        )
    }
}
