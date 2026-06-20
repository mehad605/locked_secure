package com.example.lockedsecure

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AdminReceiver : DeviceAdminReceiver() {
    
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        // ponytail: attempt to block uninstall if configured as Device Owner
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, AdminReceiver::class.java)
        if (dpm.isDeviceOwnerApp(context.packageName)) {
            try {
                dpm.setUninstallBlocked(adminComponent, context.packageName, true)
                Toast.makeText(context, "Uninstall protection activated", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Could not restrict uninstall: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
