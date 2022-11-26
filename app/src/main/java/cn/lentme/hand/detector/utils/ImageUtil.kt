package cn.lentme.hand.detector.utils

import android.graphics.*

object ImageUtil {
    fun drawCircle(bitmap: Bitmap, x: Float, y: Float, radius: Float) {
        val dx = x * bitmap.width
        val dy = y * bitmap.height
        val dr = radius * bitmap.width
        val canvas = Canvas(bitmap)
        val paint = buildPaint()
        canvas.drawCircle(dx, dy, dr, paint)
    }

    fun buildPaint(): Paint {
        val paint = Paint()
        paint.alpha = Color.alpha(0x80)
        paint.color = Color.BLUE
        paint.isAntiAlias = true
        return paint
    }

    fun createRotateBitmap(bitmap: Bitmap, rotate: Float): Bitmap {
        val matrix = Matrix()
        matrix.setRotate(rotate)
        return Bitmap.createBitmap(bitmap, 0, 0,
            bitmap.width, bitmap.height, matrix, true)
    }
}