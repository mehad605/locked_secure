package com.example.lockedsecure

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Characterization tests locking Core Contract C1:
 * package identity, zero internet permissions, exact exported components,
 * and accessibility/device-admin XML metadata.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class ManifestContractTest {
    @Test
    fun packageNameAndMinSdkMatchContract() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertEquals("com.example.lockedsecure", context.packageName)
        assertTrue(
            "minSdk must remain compatible with API 30",
            context.applicationInfo.minSdkVersion <= 30,
        )
    }

    @Test
    fun appRequestsZeroInternetOrNetworkPermissions() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val pkgInfo =
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS,
            )
        val requested = pkgInfo.requestedPermissions?.toSet().orEmpty()
        assertFalse(requested.contains(android.Manifest.permission.INTERNET))
        assertFalse(requested.contains(android.Manifest.permission.ACCESS_NETWORK_STATE))
        assertFalse(requested.contains(android.Manifest.permission.ACCESS_WIFI_STATE))
    }

    @Test
    fun manifestComponentsAndPermissionsMatchContract() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val pm = context.packageManager

        val activityInfo =
            pm.getActivityInfo(
                ComponentName(context, MainActivity::class.java),
                0,
            )
        assertTrue("MainActivity must be exported as launcher", activityInfo.exported)

        val serviceInfo =
            pm.getServiceInfo(
                ComponentName(context, PowerMenuBlockerService::class.java),
                PackageManager.GET_META_DATA,
            )
        assertFalse("PowerMenuBlockerService must not be exported", serviceInfo.exported)
        assertEquals(
            android.Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
            serviceInfo.permission,
        )
        assertNotNull(
            "PowerMenuBlockerService must declare accessibility metadata",
            serviceInfo.metaData?.getInt("android.accessibilityservice"),
        )

        val receiverInfo =
            pm.getReceiverInfo(
                ComponentName(context, AdminReceiver::class.java),
                PackageManager.GET_META_DATA,
            )
        assertTrue("AdminReceiver must be exported for DevicePolicyManager", receiverInfo.exported)
        assertEquals(
            android.Manifest.permission.BIND_DEVICE_ADMIN,
            receiverInfo.permission,
        )
        assertNotNull(
            "AdminReceiver must declare device_admin metadata",
            receiverInfo.metaData?.getInt("android.app.device_admin"),
        )
    }

    @Test
    fun accessibilityConfigMaintainsLeastPrivilege() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val resId =
            context.resources.getIdentifier(
                "accessibility_service_config",
                "xml",
                context.packageName,
            )
        assertTrue("accessibility_service_config.xml must exist", resId != 0)
        context.resources.getXml(resId).use { parser ->
            var eventType = parser.eventType
            var foundTag = false
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG && parser.name == "accessibility-service") {
                    foundTag = true
                    val canRetrieve =
                        parser.getAttributeBooleanValue(
                            "http://schemas.android.com/apk/res/android",
                            "canRetrieveWindowContent",
                            true,
                        )
                    assertFalse("canRetrieveWindowContent must remain false", canRetrieve)
                    val feedback =
                        parser.getAttributeIntValue(
                            "http://schemas.android.com/apk/res/android",
                            "accessibilityFeedbackType",
                            0,
                        )
                    assertEquals(AccessibilityServiceInfo.FEEDBACK_GENERIC, feedback)
                }
                eventType = parser.next()
            }
            assertTrue("Expected <accessibility-service> root tag", foundTag)
        }
    }
}
