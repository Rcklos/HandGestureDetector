package cn.lentme.hand.detector.detect

import android.graphics.Bitmap
import cn.lentme.hand.detector.entity.HandDetectResult
import kotlin.math.acos
import kotlin.math.sqrt

abstract class AbstractHandDetectManager() {
//    abstract fun detect(bitmap: Bitmap)
    abstract fun detect(bitmap: Bitmap): HandDetectResult

    companion object {
        protected fun isBent(angle: Double, bent: Double): Boolean {
            return angle > bent
        }
        protected fun isThumbBent(angle: Double): Boolean = isBent(angle, 53.0)
        protected fun isBent(angle: Double): Boolean = isBent(angle, 65.0)
        protected fun isStraight(angle: Double): Boolean = !isBent(angle, 49.0)

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
            if (angles.isEmpty()) GESTURE_NONE
            else if (isThumbBent(angles[0]) && isBent(angles[1]) &&
                isBent(angles[2]) && isBent(angles[3]) && isBent(angles[4]))
                GESTURE_ZERO
            else if (isThumbBent(angles[0]) && isStraight(angles[1]) &&
                isBent(angles[2]) && isBent(angles[3]) && isBent(angles[4]))
                GESTURE_ONE
            else if (isThumbBent(angles[0]) && isStraight(angles[1]) &&
                isStraight(angles[2]) && isBent(angles[3]) && isBent(angles[4]))
                GESTURE_TWO
            else if (isThumbBent(angles[0]) && isStraight(angles[1]) &&
                isStraight(angles[2]) && isStraight(angles[3]) && isBent(angles[4]))
                GESTURE_THREE
            else if (isThumbBent(angles[0]) && isStraight(angles[1]) &&
                isStraight(angles[2]) && isStraight(angles[3]) && isStraight(angles[4]))
                GESTURE_FOUR
            else if (isStraight(angles[0]) && isStraight(angles[1]) &&
                isStraight(angles[2]) && isStraight(angles[3]) && isStraight(angles[4]))
                GESTURE_FIVE
            else if(isStraight(angles[0]) && isBent(angles[1]) && isBent(angles[2]) &&
                isBent(angles[3]) && isStraight(angles[4]))
                GESTURE_SIX
            else if(isThumbBent(angles[0]) && isBent(angles[1]) && isStraight(angles[2]) &&
                isBent(angles[3]) && isBent(angles[4]))
                "友好手势"
            else if (isStraight(angles[0]) && isStraight(angles[1]) &&
                isBent(angles[2]) && isBent(angles[3]) && isBent(angles[4]))
                GESTURE_GUN
            else
                GESTURE_OTHER

        const val GESTURE_NONE  = "None"
        const val GESTURE_ZERO  = "拳头"
        const val GESTURE_ONE   = "一"
        const val GESTURE_TWO   = "二"
        const val GESTURE_THREE = "三"
        const val GESTURE_FOUR  = "四"
        const val GESTURE_FIVE  = "五"
        const val GESTURE_SIX   = "六"
        const val GESTURE_GUN   = "枪"
        const val GESTURE_OTHER = "Other"
    }
}