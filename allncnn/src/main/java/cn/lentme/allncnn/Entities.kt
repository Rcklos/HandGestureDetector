package cn.lentme.allncnn

import android.graphics.RectF

// hand
data class Point2f(val x: Float, val y: Float)
data class DetectResult(val landmark: List<List<Point2f>>)

// yolo
data class YoloObject(val rect: RectF, val label: Int, val prob: Float)
