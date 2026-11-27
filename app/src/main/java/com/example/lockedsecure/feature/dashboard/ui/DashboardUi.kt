package com.example.lockedsecure.feature.dashboard.ui

import android.view.View
import com.example.lockedsecure.feature.dashboard.domain.ProtectionStatus

/**
 * Public UI boundary for the LockedSecure dashboard screen.
 *
 * Allows swapping the visual rendering implementation (e.g., native Android Views vs. Compose)
 * without touching `MainActivity`, domain controllers, or tests.
 */
interface DashboardUi {

    /** Root view hierarchy to attach as the Activity content view. */
    val rootView: View

    /** Renders the latest [status] snapshot onto the dashboard controls. */
    fun render(status: ProtectionStatus)
}
