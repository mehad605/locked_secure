package com.example.lockedsecure

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

/**
 * Characterization tests locking Core Contract C5:
 * AdminReceiver Device Owner uninstall protection activation and error handling.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class AdminReceiverCharacterizationTest {
    private lateinit var context: Context
    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    @Before
    fun setUp() {
        ShadowToast.reset()
        context = ApplicationProvider.getApplicationContext()
        dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(context, AdminReceiver::class.java)
    }

    @Test
    fun enablesUninstallBlockAndShowsToastWhenDeviceOwner() {
        shadowOf(dpm).setDeviceOwner(adminComponent)

        AdminReceiver().onEnabled(context, Intent(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED))

        assertTrue(dpm.isUninstallBlocked(adminComponent, context.packageName))
        assertEquals("Uninstall protection activated", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun doesNothingWhenNotDeviceOwner() {
        AdminReceiver().onEnabled(context, Intent(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED))

        assertFalse(dpm.isDeviceOwnerApp(context.packageName))
        assertNull(ShadowToast.getTextOfLatestToast())
    }
}
