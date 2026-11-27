package com.example.lockedsecure.app

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import com.example.lockedsecure.AdminReceiver
import com.example.lockedsecure.PowerMenuBlockerService
import com.example.lockedsecure.R
import com.example.lockedsecure.feature.dashboard.data.AndroidProtectionStatusReader
import com.example.lockedsecure.feature.dashboard.data.AndroidSettingsNavigator
import com.example.lockedsecure.feature.dashboard.domain.DashboardController
import com.example.lockedsecure.feature.dashboard.domain.ProtectionStatusReader
import com.example.lockedsecure.feature.deviceadmin.data.AndroidDevicePolicyGateway
import com.example.lockedsecure.feature.deviceadmin.domain.DeviceAdminFactory
import com.example.lockedsecure.feature.deviceadmin.domain.UninstallProtectionUseCase
import com.example.lockedsecure.feature.powerblocker.data.AndroidLockStateProvider
import com.example.lockedsecure.feature.powerblocker.domain.PowerBlockerFactory
import com.example.lockedsecure.feature.powerblocker.domain.PowerMenuActionPolicy

/**
 * Manual dependency-injection composition root for LockedSecure.
 *
 * Uses lazy initialization so each Android entry point (Activity, AccessibilityService,
 * DeviceAdminReceiver) allocates only the collaborators it actually needs. Chosen over
 * Hilt/Koin to avoid reflection, code generation, and runtime framework size overhead.
 */
class AppGraph(context: Context) {

    private val appContext: Context = context.applicationContext

    private val accessibilityComponent: ComponentName by lazy {
        ComponentName(appContext, PowerMenuBlockerService::class.java)
    }

    private val adminComponent: ComponentName by lazy {
        ComponentName(appContext, AdminReceiver::class.java)
    }

    /** Policy deciding whether incoming accessibility window events must be dismissed. */
    val powerMenuActionPolicy: PowerMenuActionPolicy by lazy {
        PowerBlockerFactory.createPolicy(AndroidLockStateProvider(appContext))
    }

    /** Use case activating Device Owner uninstall blocking when applicable. */
    val uninstallProtectionUseCase: UninstallProtectionUseCase by lazy {
        DeviceAdminFactory.createUseCase(
            AndroidDevicePolicyGateway(appContext, adminComponent),
        )
    }

    /** Platform reader reporting Accessibility Service and Device Admin protection state. */
    val protectionStatusReader: ProtectionStatusReader by lazy {
        AndroidProtectionStatusReader(
            context = appContext,
            accessibilityComponent = accessibilityComponent,
            adminComponent = adminComponent,
        )
    }

    /**
     * Creates a [DashboardController] scoped to [activity] so Settings intents launch cleanly
     * without retaining any Activity reference inside the application graph.
     */
    fun createDashboardController(activity: Activity): DashboardController {
        val navigator = AndroidSettingsNavigator(
            activity = activity,
            adminComponent = adminComponent,
            adminExplanation = appContext.getString(R.string.device_admin_add_explanation),
        )
        return DashboardController(
            statusReader = protectionStatusReader,
            navigator = navigator,
        )
    }
}
