package cn.lentme.hand.detector.entity

import android.graphics.Bitmap
import android.graphics.RectF

data class Vector2(val x: Float, val y: Float)
data class HandDetectResult(val bitmap: Bitmap,
                            val angles: List<Double>, val points: List<Vector2>)
data class YoloDetectResult(val rect: RectF, val label: Int, val prob: Float)