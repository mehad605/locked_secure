package com.example.lockedsecure.feature.dashboard.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
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

private class StatusRowViews(
    val container: LinearLayout,
    val dot: View,
    val label: TextView,
)

private class NativeDashboardUi(
    private val context: Context,
    onOpenAccessibility: () -> Unit,
    onOpenAdmin: () -> Unit,
) : DashboardUi {

    private val shieldHalo = buildShieldHalo()
    private val shieldIcon = ShieldView(context)
    private val shieldHeadline = buildHeadlineText()
    private val powerRow = buildStatusRow()
    private val adminRow = buildStatusRow()
    private val powerButton = buildActionButton(onOpenAccessibility)
    private val adminButton = buildActionButton(onOpenAdmin)

    override val rootView: View = buildRootScrollView()

    override fun render(status: ProtectionStatus) {
        val tint = if (status.isFullyProtected) {
            DashboardTheme.ACCENT_ACTIVE
        } else {
            DashboardTheme.ACCENT_INACTIVE
        }
        shieldHalo.background = DashboardTheme.circle(DashboardTheme.withAlpha(tint, 0.08f))
        shieldIcon.setTintColor(tint)
        shieldHeadline.setText(
            if (status.isFullyProtected) R.string.status_shield_active else R.string.status_shield_offline,
        )
        bindStatusRow(powerRow, R.string.label_power_blocker, status.isAccessibilityEnabled)
        bindStatusRow(adminRow, R.string.label_uninstall_protection, status.isAdminEnabled)
        bindActionButton(
            button = powerButton,
            isActive = status.isAccessibilityEnabled,
            activeTextRes = R.string.btn_blocker_enabled,
            inactiveTextRes = R.string.btn_enable_power_blocker,
        )
        bindActionButton(
            button = adminButton,
            isActive = status.isAdminEnabled,
            activeTextRes = R.string.btn_admin_enabled,
            inactiveTextRes = R.string.btn_enable_admin_protection,
        )
    }

    private fun buildRootScrollView(): ScrollView {
        val container = buildMainColumn()
        val scrollView = ScrollView(context).apply {
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
            addView(buildHeader())
            addView(buildFlexibleSpacer())
            addView(buildShieldCard())
            addView(buildFlexibleSpacer())
            addView(buildActionButtonsColumn())
        }
    }

    private fun buildHeader(): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(0, DashboardTheme.dp(context, 16f), 0, 0)
        addView(
            TextView(context).apply {
                setText(R.string.dashboard_title)
                setTextColor(DashboardTheme.TEXT_PRIMARY)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                typeface = Typeface.create("sans-serif", Typeface.BOLD)
                letterSpacing = 0.22f
            },
        )
        addView(fixedSpacer(4f))
        addView(
            TextView(context).apply {
                setText(R.string.dashboard_subtitle)
                setTextColor(DashboardTheme.TEXT_MUTED)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                letterSpacing = 0.20f
            },
        )
    }

    private fun buildShieldCard(): LinearLayout {
        val cardHorizontal = DashboardTheme.dp(context, 24f)
        val cardVertical = DashboardTheme.dp(context, 40f)
        val iconSize = DashboardTheme.dp(context, 48f)
        shieldHalo.addView(
            shieldIcon,
            FrameLayout.LayoutParams(iconSize, iconSize, Gravity.CENTER),
        )
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = DashboardTheme.roundedRect(
                context = context,
                fillColor = DashboardTheme.CARD_BACKGROUND,
                cornerRadiusDp = 28f,
                borderWidthDp = 1f,
                borderColor = DashboardTheme.CARD_BORDER,
            )
            setPadding(cardHorizontal, cardVertical, cardHorizontal, cardVertical)
            addView(shieldHalo)
            addView(fixedSpacer(32f))
            addView(shieldHeadline)
            addView(fixedSpacer(24f))
            addView(powerRow.container)
            addView(fixedSpacer(10f))
            addView(adminRow.container)
        }
        val wrapper = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val verticalMargin = DashboardTheme.dp(context, 24f)
            setPadding(0, verticalMargin, 0, verticalMargin)
            addView(
                card,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
        }
        wrapper.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        return wrapper
    }

    private fun buildShieldHalo(): FrameLayout {
        val size = DashboardTheme.dp(context, 100f)
        return FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(size, size)
        }
    }

    private fun buildHeadlineText(): TextView = TextView(context).apply {
        setTextColor(DashboardTheme.TEXT_PRIMARY)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        letterSpacing = 0.02f
    }

    private fun buildStatusRow(): StatusRowViews {
        val dotSize = DashboardTheme.dp(context, 6f)
        val dot = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(dotSize, dotSize)
        }
        val label = TextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            addView(dot)
            addView(Space(context).apply {
                layoutParams = LinearLayout.LayoutParams(DashboardTheme.dp(context, 8f), 1)
            })
            addView(label)
        }
        return StatusRowViews(row, dot, label)
    }

    private fun buildActionButtonsColumn(): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        setPadding(0, 0, 0, DashboardTheme.dp(context, 16f))
        addView(powerButton)
        addView(fixedSpacer(12f))
        addView(adminButton)
    }

    private fun buildActionButton(onClick: () -> Unit): Button = Button(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            DashboardTheme.dp(context, 56f),
        )
        isAllCaps = false
        stateListAnimator = null
        elevation = 0f
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        letterSpacing = 0.03f
        setOnClickListener { onClick() }
    }

    private fun bindStatusRow(row: StatusRowViews, labelRes: Int, isActive: Boolean) {
        val dotColor = if (isActive) DashboardTheme.ACCENT_ACTIVE else DashboardTheme.ACCENT_INACTIVE
        row.dot.background = DashboardTheme.circle(dotColor)
        val stateText = context.getString(
            if (isActive) R.string.state_protected else R.string.state_unprotected,
        )
        row.label.text = context.getString(R.string.status_row_format, context.getString(labelRes), stateText)
        row.label.setTextColor(if (isActive) DashboardTheme.TEXT_PRIMARY else DashboardTheme.TEXT_SECONDARY)
    }

    private fun bindActionButton(
        button: Button,
        isActive: Boolean,
        activeTextRes: Int,
        inactiveTextRes: Int,
    ) {
        button.setText(if (isActive) activeTextRes else inactiveTextRes)
        button.setTextColor(if (isActive) DashboardTheme.ACCENT_ACTIVE else Color.WHITE)
        val fill = if (isActive) {
            DashboardTheme.withAlpha(DashboardTheme.ACCENT_ACTIVE, 0.12f)
        } else {
            DashboardTheme.ACCENT_INACTIVE
        }
        val borderWidth = if (isActive) 1f else 0f
        val borderColor = if (isActive) {
            DashboardTheme.withAlpha(DashboardTheme.ACCENT_ACTIVE, 0.30f)
        } else {
            Color.TRANSPARENT
        }
        button.background = DashboardTheme.ripple(
            DashboardTheme.roundedRect(context, fill, 18f, borderWidth, borderColor),
        )
    }

    private fun fixedSpacer(heightDp: Float): Space = Space(context).apply {
        layoutParams = LinearLayout.LayoutParams(1, DashboardTheme.dp(context, heightDp))
    }

    private fun buildFlexibleSpacer(): Space = Space(context).apply {
        layoutParams = LinearLayout.LayoutParams(1, 0, 1f)
    }

    private fun applySafeDrawingInsets(scrollView: ScrollView, content: LinearLayout) {
        val base = DashboardTheme.dp(context, 24f)
        scrollView.setOnApplyWindowInsetsListener { _, insets ->
            val (left, top, right, bottom) = extractSystemBarInsets(insets)
            content.setPadding(base + left, base + top, base + right, base + bottom)
            insets
        }
    }

    private fun extractSystemBarInsets(insets: WindowInsets): IntArray {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
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
}
