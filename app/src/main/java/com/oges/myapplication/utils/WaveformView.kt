package com.oges.myapplication.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt
import kotlin.math.min

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val playedPaint = Paint().apply {
        color = "#00CEC9".toColorInt() // Played color
        isAntiAlias = true
    }

    private val unplayedPaint = Paint().apply {
        color = "#A29BFE".toColorInt() // Unplayed color
        isAntiAlias = true
    }

    private var waveformData: List<Float> = emptyList()
    private var progress: Float = 0f // 0f to 1f

    private val barWidth = 12f
    private val barSpacing = 8f
    private val cornerRadius = 20f

    init {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            isForceDarkAllowed = false
        }
    }

    fun updateWaveform(data: List<Float>) {
        waveformData = data
        invalidate()
    }

    fun updateProgress(progress: Float) {
        this.progress = progress.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (waveformData.isEmpty()) return

        val viewHeight = height.toFloat()
        val centerY = viewHeight / 2

        val totalBars = waveformData.size
        val totalWidth = totalBars * (barWidth + barSpacing)

        val startX = (width - totalWidth) / 2f

        waveformData.forEachIndexed { index, value ->

            val normalized = min(1f, value)
            val barHeight = normalized * viewHeight * 0.8f

            val left = startX + index * (barWidth + barSpacing)
            val right = left + barWidth
            val top = centerY - barHeight / 2
            val bottom = centerY + barHeight / 2

            val rect = RectF(left, top, right, bottom)

            val barProgress = index.toFloat() / totalBars

            val paint = if (barProgress <= progress) {
                playedPaint
            } else {
                unplayedPaint
            }

            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        }
    }
}