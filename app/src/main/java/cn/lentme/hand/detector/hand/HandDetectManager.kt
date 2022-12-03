package cn.lentme.hand.detector.hand

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import cn.lentme.hand.detector.entity.HandDetectResult
import cn.lentme.hand.detector.entity.Vector2
import cn.lentme.mediapipe.ncnn.DetectResult
import cn.lentme.mediapipe.ncnn.NCCNHandDetector
import cn.lentme.mediapipe.ncnn.Point2f

class HandDetectManager(context: Context): AbstractHandDetectManager(context) {

    private val detector = NCCNHandDetector()

    init {
        detector.load(context.assets)
    }

    override fun detectAndDraw(bitmap: Bitmap): HandDetectResult {
        val resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val result = detector.detect(resultBitmap)
        if(result.landmark.isNotEmpty()) {
//            Log.d(TAG, "detect result size ------------> ${result.landmark.size}")
            val angles = computeHandAngle(result.landmark[0])
            val points = ArrayList<Vector2>()
            result.landmark[0].forEach {
                points.add(Vector2(it.x / bitmap.width, it.y / bitmap.height))
            }
//            result.landmark[0][8].apply {
//                Log.i(TAG, "point to ---------> (${this.x}, ${this.y})")
//            }
            return HandDetectResult(resultBitmap, angles, points)
        }
        return HandDetectResult(resultBitmap, ArrayList(), ArrayList())
    }

    companion object {
        private const val TAG = "HandDetectManager"
        private fun computeHandAngle(landmarks: List<Point2f>): List<Double> {
            val angles = ArrayList<Double>()
            if (landmarks.isEmpty()) return angles
            // 拇指
            angles.add(computeVectorAngle(
                landmarks[0].x - landmarks[2].x, landmarks[0].y - landmarks[2].y,
                landmarks[3].x - landmarks[4].x, landmarks[3].y - landmarks[4].y
            ))
            // 食指
            angles.add(computeVectorAngle(
                landmarks[0].x - landmarks[6].x, landmarks[0].y - landmarks[6].y,
                landmarks[7].x - landmarks[8].x, landmarks[7].y - landmarks[8].y
            ))
            // 中指
            angles.add(computeVectorAngle(
                landmarks[0].x - landmarks[10].x, landmarks[0].y - landmarks[10].y,
                landmarks[11].x - landmarks[12].x, landmarks[11].y - landmarks[12].y
            ))
            // 无名指
            angles.add(computeVectorAngle(
                landmarks[0].x - landmarks[14].x, landmarks[0].y - landmarks[14].y,
                landmarks[15].x - landmarks[16].x, landmarks[15].y - landmarks[16].y
            ))
            // 小指
            angles.add(computeVectorAngle(
                landmarks[0].x - landmarks[18].x, landmarks[0].y - landmarks[18].y,
                landmarks[19].x - landmarks[20].x, landmarks[19].y - landmarks[20].y
            ))
            return angles
        }
    }
}