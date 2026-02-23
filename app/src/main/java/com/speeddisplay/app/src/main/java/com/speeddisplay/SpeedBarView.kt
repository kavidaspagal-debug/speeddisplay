package com.speeddisplay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class SpeedBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val maxSpeed = 160f
    private var currentSpeed = 0f

    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#1A1A1A")
        isAntiAlias = true
    }

    private val barPaint = Paint().apply {
        isAntiAlias = true
    }

    private val cornerRadius = 12f

    fun setSpeed(speed: Float) {
        currentSpeed = speed.coerceIn(0f, maxSpeed)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Draw background track
        val bgRect = RectF(0f, 0f, w, h)
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, backgroundPaint)

        // Calculate fill height from bottom
        val fillFraction = currentSpeed / maxSpeed
        val fillHeight = h * fillFraction
        val fillTop = h - fillHeight

        if (fillHeight > 0) {
            // Green at bottom, yellow in middle, red at top
            val gradient = LinearGradient(
                0f, h,        // bottom (green)
                0f, 0f,       // top (red)
                intArrayOf(
                    Color.parseColor("#00FF00"),  // green
                    Color.parseColor("#FFFF00"),  // yellow
                    Color.parseColor("#FF2200")   // red
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            barPaint.shader = gradient

            val fillRect = RectF(0f, fillTop, w, h)
            canvas.drawRoundRect(fillRect, cornerRadius, cornerRadius, barPaint)
        }
    }
}
