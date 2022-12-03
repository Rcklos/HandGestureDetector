package cn.lentme.mediapipe.ncnn

data class Point2f(val x: Float, val y: Float)
data class DetectResult(val landmark: List<List<Point2f>>)
