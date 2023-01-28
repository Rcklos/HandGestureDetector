package cn.lentme.hand.detector.detect

import android.graphics.Bitmap
import cn.lentme.allncnn.NCNNService
import cn.lentme.hand.detector.entity.YoloDetectResult

class YoloDetectManager(private val service: NCNNService): AbstractYoloDetectManager(){

    override fun detect(bitmap: Bitmap): List<YoloDetectResult> {
        val yoloObjects = service.detectYolo(bitmap)
        val result = ArrayList<YoloDetectResult>()
        yoloObjects.forEach {
//            val rectF = RectF(
//                it.rect.left / bitmap.width,
//                it.rect.top / bitmap.height,
//                it.rect.right / bitmap.width,
//                it.rect.bottom / bitmap.height
//            )
//            result.add(YoloDetectResult(rectF, it.label, it.prob))
            result.add(YoloDetectResult(it.rect, it.label, it.prob))
        }
        return result
    }
}