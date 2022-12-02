package cn.lentme.hand.detector.hand

import android.content.Context
import android.graphics.Bitmap
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
        val angles = ArrayList<Double>()
        val points = ArrayList<Vector2>()

        detector.detect(resultBitmap, DetectResult(0));
        return HandDetectResult(resultBitmap, angles, points)
    }
}