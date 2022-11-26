package cn.lentme.mediapipe.handlandmark.data

import android.graphics.RectF

data class LocationData(
    var relativeBoundingBox: RelativeBoundingBox = RelativeBoundingBox(0f,0f,0f,0f),
    var relativeKeyPoints: MutableList<RelativeKeyPoints> = mutableListOf()
) {
    fun getRelativeBoundingBox(): RectF {
        return RectF(
            relativeBoundingBox.xMin,
            relativeBoundingBox.yMin,
            relativeBoundingBox.width,
            relativeBoundingBox.height
        )
    }
}

data class RelativeBoundingBox(
    var xMin: Float,
    var yMin: Float,
    var width: Float,
    var height: Float
)

data class RelativeKeyPoints(
    var x: Float = 0f,
    var y: Float = 0f,
    val score: Float = 0f
)
