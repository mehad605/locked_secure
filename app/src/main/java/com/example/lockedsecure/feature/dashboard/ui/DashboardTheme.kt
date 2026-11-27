package com.example.lockedsecure.feature.dashboard.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import kotlin.math.roundToInt

/**
 * Matte dark color palette and drawable helpers preserving exact visual parity with `baseline-v1`.
 */
internal object DashboardTheme {
    const val BACKGROUND = 0xFF0D0D11.toInt()
    const val CARD_BACKGROUND = 0xFF17171C.toInt()
    const val CARD_BORDER = 0xFF22222B.toInt()
    const val ACCENT_ACTIVE = 0xFF10B981.toInt()
    const val ACCENT_INACTIVE = 0xFFEF4444.toInt()
    const val TEXT_PRIMARY = 0xFFF4F4F5.toInt()
    const val TEXT_SECONDARY = 0xFFA1A1AA.toInt()
    const val TEXT_MUTED = 0xFF71717A.toInt()
    const val RIPPLE_OVERLAY = 0x33FFFFFF

    /** Converts density-independent pixels [value] to integer pixels. */
    fun dp(context: Context, value: Float): Int =
        (value * context.resources.displayMetrics.density).roundToInt()

    /** Returns [color] with its alpha channel scaled to [alphaFraction] in `0f..1f`. */
    fun withAlpha(color: Int, alphaFraction: Float): Int {
        val alpha = (255f * alphaFraction).roundToInt().coerceIn(0, 255)
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    /** Creates a rounded-rectangle drawable with optional 1dp border stroke. */
    fun roundedRect(
        context: Context,
        fillColor: Int,
        cornerRadiusDp: Float,
        borderWidthDp: Float = 0f,
        borderColor: Int = Color.TRANSPARENT,
    ): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(fillColor)
        cornerRadius = dp(context, cornerRadiusDp).toFloat()
        if (borderWidthDp > 0f) {
            setStroke(dp(context, borderWidthDp), borderColor)
        }
    }

    /** Creates an oval circle drawable filled with [fillColor]. */
    fun circle(fillColor: Int): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(fillColor)
    }

    /** Wraps [content] in a standard Android ripple drawable bounded by [content]'s shape. */
    fun ripple(content: Drawable): Drawable =
        RippleDrawable(ColorStateList.valueOf(RIPPLE_OVERLAY), content, null)
}
