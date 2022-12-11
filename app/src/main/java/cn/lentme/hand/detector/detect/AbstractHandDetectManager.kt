package cn.lentme.hand.detector.detect

import android.content.Context
import android.graphics.Bitmap
import cn.lentme.allncnn.NCNNService
import cn.lentme.hand.detector.entity.HandDetectResult
import kotlin.math.acos
import kotlin.math.sqrt

abstract class AbstractHandDetectManager() {
//    abstract fun detect(bitmap: Bitmap)
    abstract fun detectAndDraw(bitmap: Bitmap): HandDetectResult

    companion object {
        protected inline fun isBent(angle: Double, bent: Double): Boolean {
            return angle > bent
        }
        protected inline fun isThumbBent(angle: Double): Boolean = isBent(angle, 53.0)
        protected inline fun isBent(angle: Double): Boolean = isBent(angle, 65.0)
        protected inline fun isStraight(angle: Double): Boolean = !isBent(angle, 49.0)

        @JvmStatic
        protected fun computeVectorAngle(v1x: Float, v1y: Float, v2x: Float, v2y: Float): Double {
            try {
                val x = (v1x * v2x + v1y * v2y) /
                        (sqrt(v1x * v1x + v1y * v1y) * sqrt(v2x * v2x + v2y * v2y)).toDouble()
                val angle = Math.toDegrees(acos(x))
                if(angle > 180) return 65535.0
                return angle
            } catch (e: Exception) {
                return 65535.0
            }
        }

        fun computeHandGesture(angles: List<Double>): String =
            if (angles.isEmpty()) "None"
            else if (isThumbBent(angles[0]) && isBent(angles[1]) &&
                isBent(angles[2]) && isBent(angles[3]) && isBent(angles[4]))
                "拳头"
            else if (isThumbBent(angles[0]) && isStraight(angles[1]) &&
                isBent(angles[2]) && isBent(angles[3]) && isBent(angles[4]))
                "一"
            else if (isThumbBent(angles[0]) && isStraight(angles[1]) &&
                isStraight(angles[2]) && isBent(angles[3]) && isBent(angles[4]))
                "二"
            else if (isThumbBent(angles[0]) && isStraight(angles[1]) &&
                isStraight(angles[2]) && isStraight(angles[3]) && isBent(angles[4]))
                "三"
            else if (isThumbBent(angles[0]) && isStraight(angles[1]) &&
                isStraight(angles[2]) && isStraight(angles[3]) && isStraight(angles[4]))
                "四"
            else if (isStraight(angles[0]) && isStraight(angles[1]) &&
                isStraight(angles[2]) && isStraight(angles[3]) && isStraight(angles[4]))
                "五"
            else if(isStraight(angles[0]) && isBent(angles[1]) && isBent(angles[2]) &&
                isBent(angles[3]) && isStraight(angles[4]))
                "六"
            else if(isThumbBent(angles[0]) && isBent(angles[1]) && isStraight(angles[2]) &&
                isBent(angles[3]) && isBent(angles[4]))
                "友好手势"
            else if (isStraight(angles[0]) && isStraight(angles[1]) &&
                isBent(angles[2]) && isBent(angles[3]) && isBent(angles[4]))
                "枪"
            else
                "Other"
    }
}