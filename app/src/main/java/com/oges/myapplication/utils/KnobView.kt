package com.oges.myapplication.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.PI

class KnobView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var angle = 0f
    private var value = 0

    private val maxValue = 1000
    private val minAngle = -135f
    private val maxAngle = 135f

    private var listener: ((Int) -> Unit)? = null

    private val knobPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
    }

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 8f
    }

    fun setOnValueChangeListener(callback: (Int) -> Unit) {
        listener = callback
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height)
        val radius = size / 2f

        // Draw knob circle
        canvas.drawCircle(
            width / 2f,
            height / 2f,
            radius,
            knobPaint
        )

        // Draw indicator line
        val indicatorLength = radius * 0.7f
        val rad = Math.toRadians(angle.toDouble())

        val x = (width / 2 + indicatorLength * kotlin.math.cos(rad)).toFloat()
        val y = (height / 2 + indicatorLength * kotlin.math.sin(rad)).toFloat()

        canvas.drawLine(
            width / 2f,
            height / 2f,
            x,
            y,
            indicatorPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val centerX = width / 2f
        val centerY = height / 2f

        val dx = event.x - centerX
        val dy = event.y - centerY

        val touchAngle = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()

        if (touchAngle in minAngle..maxAngle) {
            angle = touchAngle

            val normalized =
                (angle - minAngle) / (maxAngle - minAngle)

            value = (normalized * maxValue).toInt()

            listener?.invoke(value)

            invalidate()
        }

        return true
    }
}