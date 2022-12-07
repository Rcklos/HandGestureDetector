package cn.lentme.hand.detector.detect

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Size
import cn.lentme.hand.detector.entity.YoloDetectResult
import cn.lentme.yolo.ncnn.YoloNCNN

class YoloDetectManager(context: Context): AbstractYoloDetectManager(context){
    private val yoloNCNN: YoloNCNN

    init {
        yoloNCNN = YoloNCNN().apply {
            load(context.assets)
        }
    }

    override fun detect(bitmap: Bitmap, screenSize: Size): List<YoloDetectResult> {
        val yoloObjects = yoloNCNN.detect(bitmap)
//        val yoloObjects = ArrayList<YoloDetectResult>()
        val result = ArrayList<YoloDetectResult>()
        yoloObjects.forEach {
            val rectF = RectF(
                it.rect.left / screenSize.width,
                it.rect.top / screenSize.height,
                it.rect.right / screenSize.width,
                it.rect.bottom / screenSize.height
            )
            result.add(YoloDetectResult(rectF, it.label, it.prob))
        }
        return result
    }
}