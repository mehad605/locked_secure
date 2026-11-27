package com.example.lockedsecure.feature.dashboard.data

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import com.example.lockedsecure.AdminReceiver
import com.example.lockedsecure.PowerMenuBlockerService
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Unit tests for [AndroidProtectionStatusReader] verifying full/short accessibility component
 * name parsing and Device Admin state detection.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class AndroidProtectionStatusReaderTest {

    private lateinit var context: Context
    private lateinit var accessibilityComponent: ComponentName
    private lateinit var adminComponent: ComponentName
    private lateinit var reader: AndroidProtectionStatusReader

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        accessibilityComponent = ComponentName(context, PowerMenuBlockerService::class.java)
        adminComponent = ComponentName(context, AdminReceiver::class.java)
        reader = AndroidProtectionStatusReader(context, accessibilityComponent, adminComponent)
    }

    @Test
    fun detectsFullAndShortFlattenedAccessibilityServiceNames() {
        Settings.Secure.putString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            "com.other/.Service:${accessibilityComponent.flattenToString()}",
        )
        assertTrue(reader.readStatus().isAccessibilityEnabled)

        Settings.Secure.putString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            accessibilityComponent.flattenToShortString(),
        )
        assertTrue(reader.readStatus().isAccessibilityEnabled)
    }

    @Test
    fun ignoresUnrelatedOrPrefixCollidingServices() {
        Settings.Secure.putString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            "${accessibilityComponent.flattenToString()}Extra:com.other/.OtherService",
        )
        assertFalse(reader.readStatus().isAccessibilityEnabled)
    }

    @Test
    fun detectsActiveDeviceAdmin() {
        assertFalse(reader.readStatus().isAdminEnabled)

        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        shadowOf(dpm).setActiveAdmin(adminComponent)
        assertTrue(reader.readStatus().isAdminEnabled)
    }
}
