package com.example.lockedsecure.feature.dashboard.data

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.example.lockedsecure.feature.dashboard.domain.ProtectionStatus
import com.example.lockedsecure.feature.dashboard.domain.ProtectionStatusReader

/**
 * Android implementation of [ProtectionStatusReader] backed by `Settings.Secure`
 * and [DevicePolicyManager].
 *
 * Parses `ENABLED_ACCESSIBILITY_SERVICES` by splitting on `:` and comparing parsed
 * [ComponentName]s rather than raw substring matching. This handles both full
 * (`pkg/pkg.Service`) and short (`pkg/.Service`) flattened names used across OEM ROMs
 * while avoiding false-positive prefix matches.
 */
class AndroidProtectionStatusReader(
    context: Context,
    private val accessibilityComponent: ComponentName,
    private val adminComponent: ComponentName,
) : ProtectionStatusReader {

    private val appContext: Context = context.applicationContext

    override fun readStatus(): ProtectionStatus = ProtectionStatus(
        isAccessibilityEnabled = isAccessibilityServiceEnabled(),
        isAdminEnabled = isDeviceAdminEnabled(),
    )

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            appContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false

        return enabledServices
            .split(':')
            .any { entry -> ComponentName.unflattenFromString(entry) == accessibilityComponent }
    }

    private fun isDeviceAdminEnabled(): Boolean {
        val manager = appContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        return manager?.isAdminActive(adminComponent) == true
    }
}
