package com.speeddisplay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class SpeedBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val maxSpeed = 160f
    private var currentSpeed = 0f
    private val scaleValues = listOf(0, 20, 40, 60, 80, 100, 120, 140, 160)

    // Convert 0.75cm to pixels
    private val barWidth = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_MM, 7.5f,
        context.resources.displayMetrics
    )

    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#1A1A1A")
        isAntiAlias = true
    }

    private val barPaint = Paint().apply {
        isAntiAlias = true
    }

    private val tickPaint = Paint().apply {
        color = Color.parseColor("#888888")
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.parseColor("#FF8800")
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 18f,
            context.resources.displayMetrics
        )
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.LEFT
    }

    private val cornerRadius = 14f
    private val tickLength = 18f
    private val textMargin = 8f

    fun setSpeed(speed: Float) {
        currentSpeed = speed.coerceIn(0f, maxSpeed)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val h = height.toFloat()

        // Draw background track
        val bgRect = RectF(0f, 0f, barWidth, h)
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, backgroundPaint)

        // Draw filled bar
        val fillFraction = currentSpeed / maxSpeed
        val fillHeight = h * fillFraction
        val fillTop = h - fillHeight

        if (fillHeight > 0) {
            val gradient = LinearGradient(
                0f, h,
                0f, 0f,
                intArrayOf(
                    Color.parseColor("#00FF00"),
                    Color.parseColor("#FFFF00"),
                    Color.parseColor("#FF2200")
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            barPaint.shader = gradient
            val fillRect = RectF(0f, fillTop, barWidth, h)
            canvas.drawRoundRect(fillRect, cornerRadius, cornerRadius, barPaint)
        }

        // Draw tick marks and labels
        for (speed in scaleValues) {
            val fraction = speed / maxSpeed
            val y = h - (h * fraction)

            // Tick mark
            canvas.drawLine(
                barWidth + 4f,
                y,
                barWidth + 4f + tickLength,
                y,
                tickPaint
            )

            // Label
            val label = speed.toString()
            val textY = y + (textPaint.textSize / 3f)
            canvas.drawText(label, barWidth + tickLength + textMargin + 4f, textY, textPaint)
        }
    }
}
