package vn.npstudio.deviceinfo.feature.screentest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

/** Draws one [ScreenTestPattern] over the whole view with Canvas only. */
class ScreenTestPatternView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var pattern: ScreenTestPattern = ScreenTestPattern.BLACK
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    // No dithering or filtering: they would hide banding and blur the 1 px checkerboard.
    private val paint = Paint().apply {
        isDither = false
        isAntiAlias = false
        isFilterBitmap = false
    }

    private var horizontalGradient: Shader? = null
    private var verticalGradient: Shader? = null

    // A 2x2 tile repeated by the GPU; 1 px squares without a full-screen bitmap.
    private val checkerboard: Shader = BitmapShader(
        Bitmap.createBitmap(
            intArrayOf(Color.BLACK, Color.WHITE, Color.WHITE, Color.BLACK),
            2,
            2,
            Bitmap.Config.ARGB_8888,
        ),
        Shader.TileMode.REPEAT,
        Shader.TileMode.REPEAT,
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        horizontalGradient = LinearGradient(0f, 0f, w.toFloat(), 0f, Color.BLACK, Color.WHITE, Shader.TileMode.CLAMP)
        verticalGradient = LinearGradient(0f, 0f, 0f, h.toFloat(), Color.BLACK, Color.WHITE, Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas) {
        when (pattern) {
            ScreenTestPattern.BLACK -> canvas.drawColor(Color.BLACK)
            ScreenTestPattern.WHITE -> canvas.drawColor(Color.WHITE)
            ScreenTestPattern.RED -> canvas.drawColor(Color.RED)
            ScreenTestPattern.GREEN -> canvas.drawColor(Color.GREEN)
            ScreenTestPattern.BLUE -> canvas.drawColor(Color.BLUE)
            ScreenTestPattern.GRAY -> canvas.drawColor(GRAY_50)
            ScreenTestPattern.GRADIENT_HORIZONTAL -> fill(canvas, horizontalGradient)
            ScreenTestPattern.GRADIENT_VERTICAL -> fill(canvas, verticalGradient)
            ScreenTestPattern.CHECKERBOARD -> fill(canvas, checkerboard)
        }
    }

    private fun fill(canvas: Canvas, shader: Shader?) {
        paint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private companion object {
        const val GRAY_50 = 0xFF808080.toInt()
    }
}
