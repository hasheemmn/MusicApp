package com.oges.myapplication.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = "#6200EE".toColorInt()
        strokeWidth = 4f
        isAntiAlias = true
    }

    init {
        // Disable Force Dark to prevent crashes on Xiaomi/POCO devices
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            isForceDarkAllowed = false
        }
    }

    private var waveformData: List<Float> = emptyList()
    private var progress: Float = 0f // 0.0 to 1.0

    fun updateWaveform(data: List<Float>) {
        this.waveformData = data
        invalidate()
    }

    fun updateProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (waveformData.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val centerY = h / 2
        val barCount = waveformData.size
        val barWidth = w / barCount

        waveformData.forEachIndexed { index, value ->
            val x = index * barWidth
            val barHeight = value * h * 0.8f
            
            // Highlight progress
            if (index.toFloat() / barCount <= progress) {
                paint.color = "#00CEC9".toColorInt() // Accent Cyan
            } else {
                paint.color = "#A29BFE".toColorInt() // Primary Light Purple
            }

            canvas.drawLine(x, centerY - barHeight / 2, x, centerY + barHeight / 2, paint)
        }
    }
}
