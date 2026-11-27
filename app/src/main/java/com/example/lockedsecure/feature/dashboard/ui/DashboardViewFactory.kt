package com.example.lockedsecure.feature.dashboard.ui

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.LinearLayout
import android.widget.ScrollView
import com.example.lockedsecure.R
import com.example.lockedsecure.feature.dashboard.domain.ProtectionStatus

/**
 * Factory creating the pure Android View implementation of [DashboardUi].
 *
 * Renders the exact matte-dark layout from `baseline-v1` with zero Jetpack Compose or AndroidX
 * dependencies, keeping APK size minimal and idle memory near the ART process floor.
 */
object DashboardViewFactory {
    /**
     * Builds a [DashboardUi] attached to [context] with click callbacks for both settings actions.
     */
    fun create(
        context: Context,
        onOpenAccessibility: () -> Unit,
        onOpenAdmin: () -> Unit,
    ): DashboardUi = NativeDashboardUi(context, onOpenAccessibility, onOpenAdmin)
}

private class NativeDashboardUi(
    private val context: Context,
    onOpenAccessibility: () -> Unit,
    onOpenAdmin: () -> Unit,
) : DashboardUi {
    private val shieldIcon = ShieldView(context)
    private val shieldHalo = DashboardComponents.buildShieldHalo(context, shieldIcon)
    private val shieldHeadline = DashboardComponents.buildHeadlineText(context)
    private val powerRow = DashboardComponents.buildStatusRow(context)
    private val adminRow = DashboardComponents.buildStatusRow(context)
    private val powerButton = DashboardComponents.buildActionButton(context, onOpenAccessibility)
    private val adminButton = DashboardComponents.buildActionButton(context, onOpenAdmin)

    override val rootView: View = buildRootScrollView()

    override fun render(status: ProtectionStatus) {
        val tint =
            if (status.isFullyProtected) {
                DashboardTheme.ACCENT_ACTIVE
            } else {
                DashboardTheme.ACCENT_INACTIVE
            }
        shieldHalo.background = DashboardTheme.circle(DashboardTheme.withAlpha(tint, 0.08f))
        shieldIcon.setTintColor(tint)
        shieldHeadline.setText(
            if (status.isFullyProtected) R.string.status_shield_active else R.string.status_shield_offline,
        )
        DashboardComponents.bindStatusRow(
            context = context,
            row = powerRow,
            labelRes = R.string.label_power_blocker,
            isActive = status.isAccessibilityEnabled,
        )
        DashboardComponents.bindStatusRow(
            context = context,
            row = adminRow,
            labelRes = R.string.label_uninstall_protection,
            isActive = status.isAdminEnabled,
        )
        DashboardComponents.bindActionButton(
            context = context,
            button = powerButton,
            isActive = status.isAccessibilityEnabled,
            activeTextRes = R.string.btn_blocker_enabled,
            inactiveTextRes = R.string.btn_enable_power_blocker,
        )
        DashboardComponents.bindActionButton(
            context = context,
            button = adminButton,
            isActive = status.isAdminEnabled,
            activeTextRes = R.string.btn_admin_enabled,
            inactiveTextRes = R.string.btn_enable_admin_protection,
        )
    }

    private fun buildRootScrollView(): ScrollView {
        val container = buildMainColumn()
        val scrollView =
            ScrollView(context).apply {
                isFillViewport = true
                setBackgroundColor(DashboardTheme.BACKGROUND)
                addView(
                    container,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ),
                )
            }
        applySafeDrawingInsets(scrollView, container)
        return scrollView
    }

    private fun buildMainColumn(): LinearLayout {
        val basePadding = DashboardTheme.dp(context, 24f)
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(basePadding, basePadding, basePadding, basePadding)
            addView(DashboardComponents.buildHeader(context))
            addView(DashboardComponents.flexibleSpacer(context))
            addView(buildShieldCardSection())
            addView(DashboardComponents.flexibleSpacer(context))
            addView(buildActionButtonsColumn())
        }
    }

    private fun buildShieldCardSection(): LinearLayout {
        val verticalMargin = DashboardTheme.dp(context, 24f)
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = DashboardComponents.matchWidthParams()
            setPadding(0, verticalMargin, 0, verticalMargin)
            addView(buildShieldCard(), DashboardComponents.matchWidthParams())
        }
    }

    private fun buildShieldCard(): LinearLayout {
        val horizontal = DashboardTheme.dp(context, 24f)
        val vertical = DashboardTheme.dp(context, 40f)
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background =
                DashboardTheme.roundedRect(
                    context = context,
                    fillColor = DashboardTheme.CARD_BACKGROUND,
                    cornerRadiusDp = 28f,
                    borderWidthDp = 1f,
                    borderColor = DashboardTheme.CARD_BORDER,
                )
            setPadding(horizontal, vertical, horizontal, vertical)
            addView(shieldHalo)
            addView(DashboardComponents.fixedSpacer(context, 32f))
            addView(shieldHeadline)
            addView(DashboardComponents.fixedSpacer(context, 24f))
            addView(powerRow.container)
            addView(DashboardComponents.fixedSpacer(context, 10f))
            addView(adminRow.container)
        }
    }

    private fun buildActionButtonsColumn(): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = DashboardComponents.matchWidthParams()
            setPadding(0, 0, 0, DashboardTheme.dp(context, 16f))
            addView(powerButton)
            addView(DashboardComponents.fixedSpacer(context, 12f))
            addView(adminButton)
        }

    private fun applySafeDrawingInsets(
        scrollView: ScrollView,
        content: LinearLayout,
    ) {
        val base = DashboardTheme.dp(context, 24f)
        scrollView.setOnApplyWindowInsetsListener { _, insets ->
            val bars = extractSystemBarInsets(insets)
            content.setPadding(base + bars[0], base + bars[1], base + bars[2], base + bars[3])
            insets
        }
    }

    private fun extractSystemBarInsets(insets: WindowInsets): IntArray =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bars =
                insets.getInsets(
                    WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout(),
                )
            intArrayOf(bars.left, bars.top, bars.right, bars.bottom)
        } else {
            @Suppress("DEPRECATION")
            intArrayOf(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                insets.systemWindowInsetBottom,
            )
        }
}
