package com.example.lockedsecure

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.lockedsecure.feature.deviceadmin.data.AndroidDevicePolicyGateway
import com.example.lockedsecure.feature.deviceadmin.domain.DeviceAdminFactory
import com.example.lockedsecure.feature.deviceadmin.domain.UninstallProtectionResult

/**
 * Device administrator receiver entry point protecting LockedSecure against casual uninstallation.
 *
 * Kept at `com.example.lockedsecure.AdminReceiver` to preserve manifest component identity
 * across app upgrades so active Device Admin / Device Owner registration is never lost.
 */
class AdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        val gateway = AndroidDevicePolicyGateway(
            context = context,
            adminComponent = ComponentName(context, AdminReceiver::class.java),
        )
        val useCase = DeviceAdminFactory.createUseCase(gateway)
        when (val result = useCase.activateIfDeviceOwner()) {
            UninstallProtectionResult.Activated -> {
                Toast.makeText(
                    context,
                    R.string.toast_uninstall_protection_activated,
                    Toast.LENGTH_SHORT,
                ).show()
            }
            UninstallProtectionResult.NotDeviceOwner -> Unit
            is UninstallProtectionResult.Failed -> {
                val message = context.getString(
                    R.string.toast_uninstall_restriction_failed,
                    result.reason.orEmpty(),
                )
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
