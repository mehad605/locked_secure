package com.example.lockedsecure.feature.dashboard.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/**
 * Custom Android [View] rendering the LockedSecure cubic-Bézier shield outline.
 *
 * Pre-allocates [shieldPath] and [strokePaint] and only rebuilds path geometry in [onSizeChanged]
 * so [onDraw] performs zero heap allocations.
 */
internal class ShieldView(context: Context) : View(context) {

    private val shieldPath = Path()
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = DashboardTheme.dp(context, 3f).toFloat()
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = DashboardTheme.ACCENT_INACTIVE
    }

    /**
     * Updates the shield outline stroke color and triggers a redraw only when changed.
     */
    fun setTintColor(color: Int) {
        if (strokePaint.color != color) {
            strokePaint.color = color
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildShieldPath(w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(shieldPath, strokePaint)
    }

    private fun rebuildShieldPath(width: Float, height: Float) {
        shieldPath.reset()
        if (width <= 0f || height <= 0f) return
        shieldPath.moveTo(width * 0.5f, height * 0.1f)
        shieldPath.cubicTo(
            width * 0.85f, height * 0.1f,
            width * 0.9f, height * 0.25f,
            width * 0.9f, height * 0.45f,
        )
        shieldPath.cubicTo(
            width * 0.9f, height * 0.72f,
            width * 0.5f, height * 0.92f,
            width * 0.5f, height * 0.92f,
        )
        shieldPath.cubicTo(
            width * 0.5f, height * 0.92f,
            width * 0.1f, height * 0.72f,
            width * 0.1f, height * 0.45f,
        )
        shieldPath.cubicTo(
            width * 0.1f, height * 0.25f,
            width * 0.15f, height * 0.1f,
            width * 0.5f, height * 0.1f,
        )
        shieldPath.close()
    }
}
