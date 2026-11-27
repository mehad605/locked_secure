package com.example.lockedsecure.feature.dashboard.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import com.example.lockedsecure.R

/**
 * Holds references to a dashboard status row's indicator dot and label text.
 */
internal class StatusRowViews(
    val container: LinearLayout,
    val dot: View,
    val label: TextView,
)

/**
 * Reusable pure-Android View builders for the dashboard header, shield card, status rows,
 * and action buttons. Kept separate from [DashboardViewFactory] so each file and function
 * stays compact and focused on a single visual concern.
 */
internal object DashboardComponents {
    fun buildHeader(context: Context): LinearLayout =
        LinearLayout(context).apply {
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
            addView(fixedSpacer(context, 4f))
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

    fun buildShieldHalo(
        context: Context,
        shieldIcon: ShieldView,
    ): FrameLayout {
        val haloSize = DashboardTheme.dp(context, 100f)
        val iconSize = DashboardTheme.dp(context, 48f)
        return FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(haloSize, haloSize)
            addView(shieldIcon, FrameLayout.LayoutParams(iconSize, iconSize, Gravity.CENTER))
        }
    }

    fun buildHeadlineText(context: Context): TextView =
        TextView(context).apply {
            setTextColor(DashboardTheme.TEXT_PRIMARY)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            letterSpacing = 0.02f
        }

    fun buildStatusRow(context: Context): StatusRowViews {
        val dotSize = DashboardTheme.dp(context, 6f)
        val dot =
            View(context).apply {
                layoutParams = LinearLayout.LayoutParams(dotSize, dotSize)
            }
        val label =
            TextView(context).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            }
        val row =
            LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = matchWidthParams()
                addView(dot)
                addView(
                    Space(context).apply {
                        layoutParams = LinearLayout.LayoutParams(DashboardTheme.dp(context, 8f), 1)
                    },
                )
                addView(label)
            }
        return StatusRowViews(row, dot, label)
    }

    fun buildActionButton(
        context: Context,
        onClick: () -> Unit,
    ): Button =
        Button(context).apply {
            layoutParams =
                LinearLayout.LayoutParams(
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

    fun bindStatusRow(
        context: Context,
        row: StatusRowViews,
        labelRes: Int,
        isActive: Boolean,
    ) {
        val dotColor = if (isActive) DashboardTheme.ACCENT_ACTIVE else DashboardTheme.ACCENT_INACTIVE
        row.dot.background = DashboardTheme.circle(dotColor)
        val stateText =
            context.getString(
                if (isActive) R.string.state_protected else R.string.state_unprotected,
            )
        row.label.text =
            context.getString(
                R.string.status_row_format,
                context.getString(labelRes),
                stateText,
            )
        row.label.setTextColor(
            if (isActive) DashboardTheme.TEXT_PRIMARY else DashboardTheme.TEXT_SECONDARY,
        )
    }

    fun bindActionButton(
        context: Context,
        button: Button,
        isActive: Boolean,
        activeTextRes: Int,
        inactiveTextRes: Int,
    ) {
        button.setText(if (isActive) activeTextRes else inactiveTextRes)
        button.setTextColor(if (isActive) DashboardTheme.ACCENT_ACTIVE else Color.WHITE)
        val fill =
            if (isActive) {
                DashboardTheme.withAlpha(DashboardTheme.ACCENT_ACTIVE, 0.12f)
            } else {
                DashboardTheme.ACCENT_INACTIVE
            }
        val borderWidth = if (isActive) 1f else 0f
        val borderColor =
            if (isActive) {
                DashboardTheme.withAlpha(DashboardTheme.ACCENT_ACTIVE, 0.30f)
            } else {
                Color.TRANSPARENT
            }
        button.background =
            DashboardTheme.ripple(
                DashboardTheme.roundedRect(context, fill, 18f, borderWidth, borderColor),
            )
    }

    fun fixedSpacer(
        context: Context,
        heightDp: Float,
    ): Space =
        Space(context).apply {
            layoutParams = LinearLayout.LayoutParams(1, DashboardTheme.dp(context, heightDp))
        }

    fun flexibleSpacer(context: Context): Space =
        Space(context).apply {
            layoutParams = LinearLayout.LayoutParams(1, 0, 1f)
        }

    fun matchWidthParams(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
}
