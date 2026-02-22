package com.speeddisplay

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView

class SettingsDialog(
    context: Context,
    private val currentColor: Int,
    private val onColorSelected: (Int) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1A1A"))
            setPadding(60, 60, 60, 60)
        }

        val title = TextView(context).apply {
            text = "DISPLAY COLOR"
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }
        layout.addView(title)

        val colors = listOf(
            Triple(MainActivity.COLOR_GREEN, "#00FF00", "GREEN"),
            Triple(MainActivity.COLOR_BLUE, "#00AAFF", "BLUE"),
            Triple(MainActivity.COLOR_RED, "#FF2200", "RED"),
            Triple(MainActivity.COLOR_YELLOW, "#FFEE00", "YELLOW")
        )

        for ((colorId, hex, name) in colors) {
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 16, 0, 16)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    onColorSelected(colorId)
                    dismiss()
                }
            }

            val swatch = View(context).apply {
                layoutParams = ViewGroup.LayoutParams(48, 48)
                val drawable = GradientDrawable()
                drawable.shape = GradientDrawable.OVAL
                drawable.setColor(Color.parseColor(hex))
                background = drawable
            }

            val label = TextView(context).apply {
                text = if (colorId == currentColor) "● $name" else "  $name"
                textSize = 20f
                setTextColor(Color.parseColor(hex))
                setPadding(30, 0, 0, 0)
            }

            row.addView(swatch)
            row.addView(label)
            layout.addView(row)
        }

        val closeBtn = TextView(context).apply {
            text = "CLOSE"
            textSize = 16f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 0)
            setOnClickListener { dismiss() }
        }
        layout.addView(closeBtn)

        setContentView(layout)
        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
    }
}
