package com.example.lockedsecure

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Characterization tests locking Core Contracts C6, C7, and C8 at the Activity lifecycle level:
 * MainActivity launches and resumes cleanly across all four protection-state combinations.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class MainActivityCharacterizationTest {
    @Test
    fun launchesAndResumesAcrossAllProtectionStates() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertNotNull(activity)

        val dpm = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(activity, AdminReceiver::class.java)
        val serviceFlat = ComponentName(activity, PowerMenuBlockerService::class.java).flattenToString()

        // State 1: Neither enabled (default)
        controller.pause().resume()

        // State 2: Accessibility only
        Settings.Secure.putString(
            activity.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            serviceFlat,
        )
        controller.pause().resume()

        // State 3: Both enabled
        shadowOf(dpm).setActiveAdmin(admin)
        controller.pause().resume()

        // State 4: Admin only
        Settings.Secure.putString(
            activity.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            "",
        )
        controller.pause().resume()

        controller.destroy()
    }
}
