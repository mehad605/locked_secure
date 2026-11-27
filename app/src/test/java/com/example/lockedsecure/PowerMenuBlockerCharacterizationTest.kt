package com.example.lockedsecure

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAccessibilityService

/**
 * Characterization tests locking Core Contracts C2, C3, and C4:
 * power-menu detection heuristics, keyguard lock-state gating, null safety, and onInterrupt.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class PowerMenuBlockerCharacterizationTest {
    private lateinit var service: PowerMenuBlockerService
    private lateinit var shadowService: ShadowAccessibilityService
    private lateinit var keyguardManager: KeyguardManager

    @Before
    fun setUp() {
        service = Robolectric.buildService(PowerMenuBlockerService::class.java).create().get()
        shadowService = shadowOf(service)
        keyguardManager = service.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    }

    private fun event(
        type: Int = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
        pkg: String? = "com.android.systemui",
        cls: String? = "com.android.systemui.globalactions.GlobalActionsDialog",
    ): AccessibilityEvent =
        AccessibilityEvent(type).apply {
            packageName = pkg
            className = cls
        }

    @Test
    fun blocksStandardGlobalActionsDialogWhenLocked() {
        shadowOf(keyguardManager).setKeyguardLocked(true)

        service.onAccessibilityEvent(event())

        assertEquals(
            listOf(AccessibilityService.GLOBAL_ACTION_BACK),
            shadowService.globalActionsPerformed,
        )
    }

    @Test
    fun blocksCustomOemGlobalActionsCaseInsensitiveWhenLocked() {
        shadowOf(keyguardManager).setKeyguardLocked(true)

        service.onAccessibilityEvent(
            event(pkg = "com.coloros.systemui", cls = "com.oplus.globalactions.PowerSheet"),
        )

        assertEquals(
            listOf(AccessibilityService.GLOBAL_ACTION_BACK),
            shadowService.globalActionsPerformed,
        )
    }

    @Test
    fun blocksSystemUiGlobalDialogVariantWhenLocked() {
        shadowOf(keyguardManager).setKeyguardLocked(true)

        service.onAccessibilityEvent(
            event(pkg = "com.android.systemui", cls = "com.android.systemui.GlobalPowerDialog"),
        )

        assertEquals(
            listOf(AccessibilityService.GLOBAL_ACTION_BACK),
            shadowService.globalActionsPerformed,
        )
    }

    @Test
    fun allowsPowerMenuWhenUnlocked() {
        shadowOf(keyguardManager).setKeyguardLocked(false)

        service.onAccessibilityEvent(event())

        assertTrue(shadowService.globalActionsPerformed.isEmpty())
    }

    @Test
    fun ignoresNonPowerMenuWindowsEvenWhenLocked() {
        shadowOf(keyguardManager).setKeyguardLocked(true)

        service.onAccessibilityEvent(
            event(pkg = "com.android.settings", cls = "com.android.settings.SettingsActivity"),
        )
        service.onAccessibilityEvent(
            event(pkg = "com.android.systemui", cls = "com.android.systemui.VolumeDialog"),
        )
        service.onAccessibilityEvent(
            event(pkg = "com.example.other", cls = "com.example.GlobalDialog"),
        )

        assertTrue(shadowService.globalActionsPerformed.isEmpty())
    }

    @Test
    fun ignoresNonWindowStateChangedEvents() {
        shadowOf(keyguardManager).setKeyguardLocked(true)

        service.onAccessibilityEvent(
            event(type = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED),
        )

        assertTrue(shadowService.globalActionsPerformed.isEmpty())
    }

    @Test
    fun handlesNullPackageAndClassNamesSafely() {
        shadowOf(keyguardManager).setKeyguardLocked(true)

        service.onAccessibilityEvent(event(pkg = null, cls = null))
        service.onAccessibilityEvent(event(pkg = "com.android.systemui", cls = null))
        service.onAccessibilityEvent(event(pkg = null, cls = "com.android.systemui.GlobalDialog"))

        assertTrue(shadowService.globalActionsPerformed.isEmpty())
    }

    @Test
    fun onInterruptIsSafeNoOp() {
        service.onInterrupt()
        assertTrue(shadowService.globalActionsPerformed.isEmpty())
    }
}
