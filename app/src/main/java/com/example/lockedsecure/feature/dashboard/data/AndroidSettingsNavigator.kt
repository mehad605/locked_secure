package com.example.lockedsecure.feature.dashboard.data

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import com.example.lockedsecure.feature.dashboard.domain.NavigationResult
import com.example.lockedsecure.feature.dashboard.domain.SettingsNavigator
import com.example.lockedsecure.feature.dashboard.domain.SettingsTarget

/**
 * Android implementation of [SettingsNavigator] launching system intents from an [Activity].
 */
class AndroidSettingsNavigator(
    private val activity: Activity,
    private val adminComponent: ComponentName,
    private val adminExplanation: String,
) : SettingsNavigator {
    override fun openAccessibilitySettings(): NavigationResult =
        launchIntent(
            intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
            target = SettingsTarget.ACCESSIBILITY,
        )

    override fun openDeviceAdminSettings(): NavigationResult {
        val intent =
            Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, adminExplanation)
            }
        return launchIntent(intent = intent, target = SettingsTarget.DEVICE_ADMIN)
    }

    private fun launchIntent(
        intent: Intent,
        target: SettingsTarget,
    ): NavigationResult =
        try {
            activity.startActivity(intent)
            NavigationResult.Opened
        } catch (_: ActivityNotFoundException) {
            NavigationResult.Unavailable(target)
        } catch (_: SecurityException) {
            NavigationResult.Unavailable(target)
        }
}
