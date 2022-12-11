package cn.lentme.hand.detector.utils

import android.graphics.*
import android.util.Size
import androidx.core.view.ContentInfoCompat.Flags

object ImageUtil {

    fun createBitmap(src: Bitmap, rectF: RectF): Bitmap? {
        try {
            val x = rectF.left * src.width
            val y = rectF.top * src.height
            val width = (rectF.right - rectF.left) * src.width
            val height = (rectF.bottom - rectF.top) * src.height
            return Bitmap.createBitmap(src, x.toInt(), y.toInt(), width.toInt(), height.toInt())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun drawArc(canvas: Canvas, size: Size, x: Float, y: Float, radius: Float, endAngle: Float) {
        val dr = radius * size.width
        val left = x * size.width - dr
        val top = y * size.height - dr
        val right = x * size.width + dr
        val bottom = y * size.height + dr
        val paint = buildPaint()
        canvas.drawArc(left, top, right, bottom, 0f, endAngle, false, paint)
    }

    fun drawRect(canvas: Canvas, size: Size, rectF: RectF) {
        val dRectF = RectF(
            rectF.left * size.width,
            rectF.top * size.height,
            rectF.right * size.width,
            rectF.bottom * size.height
        )
        canvas.drawRect(dRectF, buildPaint())
    }

    private fun buildPaint(): Paint {
        val paint = Paint()
        paint.alpha = Color.alpha(0x80)
        paint.color = Color.BLUE
        paint.isAntiAlias = true
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE
        return paint
    }

    fun createRotateBitmap(bitmap: Bitmap, rotate: Float): Bitmap {
        val matrix = Matrix()
        matrix.setRotate(rotate)
        return Bitmap.createBitmap(bitmap, 0, 0,
            bitmap.width, bitmap.height, matrix, true)
    }

    fun markYolo(bitmap: Bitmap, rectF: RectF, label: String) {
        val size = Size(bitmap.width, bitmap.height)
        val dRectF = RectF(
            rectF.left * size.width,
            rectF.top * size.height,
            rectF.right * size.width,
            rectF.bottom * size.height
        )

        val canvas = Canvas(bitmap)
        val paint = buildPaint()
        paint.color = Color.GREEN
        canvas.drawRect(dRectF, paint)
        paint.color = Color.RED
        paint.textSize = 16f
        paint.strokeWidth = 1f
        canvas.drawText(label, dRectF.left, dRectF.top, paint)
    }
}