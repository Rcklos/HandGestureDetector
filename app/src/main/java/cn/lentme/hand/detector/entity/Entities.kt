package cn.lentme.hand.detector.entity

import android.graphics.Bitmap

data class Vector2(val x: Float, val y: Float)
data class HandDetectResult(val bitmap: Bitmap,
                            val angles: List<Double>, val points: List<Vector2>)