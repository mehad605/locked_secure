package com.example.lockedsecure.feature.deviceadmin.data

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.example.lockedsecure.feature.deviceadmin.domain.DevicePolicyGateway

/**
 * Android implementation of [DevicePolicyGateway] backed by [DevicePolicyManager].
 *
 * Accepts the admin [ComponentName] explicitly via constructor injection so data layer code
 * does not depend on the top-level receiver class directly.
 */
class AndroidDevicePolicyGateway(
    context: Context,
    private val adminComponent: ComponentName,
) : DevicePolicyGateway {
    private val appContext: Context = context.applicationContext

    private fun devicePolicyManager(): DevicePolicyManager? =
        appContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager

    override fun isDeviceOwner(): Boolean = devicePolicyManager()?.isDeviceOwnerApp(appContext.packageName) == true

    override fun blockAppUninstall(): Result<Unit> {
        val manager =
            devicePolicyManager()
                ?: return Result.failure(IllegalStateException("DevicePolicyManager unavailable"))
        return try {
            manager.setUninstallBlocked(adminComponent, appContext.packageName, true)
            Result.success(Unit)
        } catch (error: SecurityException) {
            Result.failure(error)
        } catch (error: IllegalArgumentException) {
            Result.failure(error)
        }
    }
}
