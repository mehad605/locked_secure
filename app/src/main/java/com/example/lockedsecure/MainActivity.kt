package com.example.lockedsecure

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import com.example.lockedsecure.app.LockedSecureApp
import com.example.lockedsecure.feature.dashboard.domain.DashboardController
import com.example.lockedsecure.feature.dashboard.domain.NavigationResult
import com.example.lockedsecure.feature.dashboard.domain.SettingsTarget
import com.example.lockedsecure.feature.dashboard.ui.DashboardUi
import com.example.lockedsecure.feature.dashboard.ui.DashboardViewFactory

/**
 * Main launcher [Activity] displaying the LockedSecure anti-theft shield dashboard.
 *
 * Kept at `com.example.lockedsecure.MainActivity` to preserve launcher shortcuts and upgrade
 * compatibility. Delegates state queries and navigation to [DashboardController] and UI
 * rendering to [DashboardUi].
 */
class MainActivity : Activity() {
    private var controller: DashboardController? = null
    private var dashboardUi: DashboardUi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activeController = LockedSecureApp.graph(this).createDashboardController(this)
        val ui =
            DashboardViewFactory.create(
                context = this,
                onOpenAccessibility = { handleNavigation(activeController.onPowerBlockerAction()) },
                onOpenAdmin = { handleNavigation(activeController.onAdminProtectionAction()) },
            )
        controller = activeController
        dashboardUi = ui
        setContentView(ui.rootView)
        configureEdgeToEdge()
        ui.render(activeController.loadStatus())
    }

    override fun onResume() {
        super.onResume()
        val activeController = controller ?: return
        dashboardUi?.render(activeController.loadStatus())
    }

    override fun onDestroy() {
        controller = null
        dashboardUi = null
        super.onDestroy()
    }

    private fun handleNavigation(result: NavigationResult) {
        if (result is NavigationResult.Unavailable) {
            val messageRes =
                when (result.target) {
                    SettingsTarget.ACCESSIBILITY -> R.string.toast_accessibility_settings_unavailable
                    SettingsTarget.DEVICE_ADMIN -> R.string.toast_admin_settings_unavailable
                }
            Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
        }
    }

    private fun configureEdgeToEdge() {
        @Suppress("DEPRECATION")
        run {
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Needed on API 30-34; API 35+ enables edge-to-edge by default.
            @Suppress("DEPRECATION")
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
    }
}
