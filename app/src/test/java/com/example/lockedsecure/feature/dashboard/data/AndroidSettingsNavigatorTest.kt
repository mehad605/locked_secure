package com.example.lockedsecure.feature.dashboard.data

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import com.example.lockedsecure.AdminReceiver
import com.example.lockedsecure.feature.dashboard.domain.NavigationResult
import com.example.lockedsecure.feature.dashboard.domain.SettingsTarget
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Unit tests for [AndroidSettingsNavigator] locking Core Contract C8:
 * exact Settings intent actions, Device Admin extras, and ActivityNotFoundException handling.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class AndroidSettingsNavigatorTest {
    @Test
    fun launchesAccessibilityAndDeviceAdminIntentsWithExpectedExtras() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val admin = ComponentName(activity, AdminReceiver::class.java)
        val explanation = "Protects LockedSecure from deletion."
        val navigator = AndroidSettingsNavigator(activity, admin, explanation)

        assertEquals(NavigationResult.Opened, navigator.openAccessibilitySettings())
        val accessibilityIntent = shadowOf(activity).nextStartedActivity
        assertEquals(Settings.ACTION_ACCESSIBILITY_SETTINGS, accessibilityIntent.action)

        assertEquals(NavigationResult.Opened, navigator.openDeviceAdminSettings())
        val adminIntent = shadowOf(activity).nextStartedActivity
        assertEquals(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN, adminIntent.action)
        @Suppress("DEPRECATION")
        assertEquals(
            admin,
            adminIntent.getParcelableExtra<ComponentName>(DevicePolicyManager.EXTRA_DEVICE_ADMIN),
        )
        assertEquals(
            explanation,
            adminIntent.getStringExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION),
        )
    }

    @Test
    fun returnsUnavailableWhenSettingsActivityCannotBeStarted() {
        val failingActivity =
            object : Activity() {
                override fun startActivity(intent: Intent?): Unit = throw ActivityNotFoundException("No handler")
            }
        val admin = ComponentName("com.example.lockedsecure", AdminReceiver::class.java.name)
        val navigator = AndroidSettingsNavigator(failingActivity, admin, "explanation")

        assertEquals(
            NavigationResult.Unavailable(SettingsTarget.ACCESSIBILITY),
            navigator.openAccessibilitySettings(),
        )
        assertEquals(
            NavigationResult.Unavailable(SettingsTarget.DEVICE_ADMIN),
            navigator.openDeviceAdminSettings(),
        )
    }
}
