package cn.lentme.yolo.ncnn

import android.graphics.RectF

data class YoloObject(val rect: RectF, val label: Int, val prob: Float)