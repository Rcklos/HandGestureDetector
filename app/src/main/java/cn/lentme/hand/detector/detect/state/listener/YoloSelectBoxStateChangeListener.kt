package cn.lentme.hand.detector.detect.state.listener

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import cn.lentme.hand.detector.app.App
import cn.lentme.hand.detector.app.TtsManager
import cn.lentme.hand.detector.detect.AbstractHandDetectManager
import cn.lentme.hand.detector.detect.AbstractYoloDetectManager
import cn.lentme.hand.detector.detect.YoloDetectManager
import cn.lentme.hand.detector.detect.state.DefaultStateChangeListener
import cn.lentme.hand.detector.detect.state.HandState
import cn.lentme.hand.detector.entity.Vector2
import cn.lentme.hand.detector.util.CommonUtils
import cn.lentme.hand.detector.util.ImageUtil
import org.koin.java.KoinJavaComponent.inject
import kotlin.math.pow
import kotlin.math.sqrt

class YoloSelectBoxStateChangeListener: DefaultStateChangeListener() {

    private val detectManager: YoloDetectManager
        by inject(YoloDetectManager::class.java)

    private val ttsManager: TtsManager
        by inject(TtsManager::class.java)

    companion object {
        private val MAX_YOLO_DISPLAY_TIME = 1500L
    }

    private object Context {
        var beginX: Float = 0.0f
        var beginY: Float = 0.0f
        var pointX: Float = 0.0f
        var pointY: Float = 0.0f

        var  rectF: RectF?      = null
        var  label: String      = ""
        var startT: Long        = 0L
        var isDetected: Boolean = false
    }

    override fun changed(duration: Long, updateData: HandState.UpdateData): Boolean = Context.run {
        val result = updateData.result
        val process= duration.toFloat() / TRIGGER_EVENT_TIME_REFERENCE * 360
        if(result.points.isNotEmpty()) {
            pointX = result.points[8].x
            pointY = result.points[8].y
        }
        drawArc(updateData.bitmap, pointX, pointY, 0.08f, process)
        if(duration >= TRIGGER_EVENT_TIME_REFERENCE) {
            beginX = pointX
            beginY = pointY
            runOnUiThread {
                ttsManager.speakText("开始框选")
            }
            return@run true
        }
        return@run false
    }

    private fun reset() = Context.apply {
        beginX = 0.0f
        beginY = 0.0f
        pointX = 0.0f
        pointY = 0.0f

        startT = 0L
        isDetected = false
        rectF = null
        label = ""
    }

    override fun consume(updateData: HandState.UpdateData): Boolean = Context.run {
        val bitmap = updateData.bitmap
        val isNotCancel = super.consume(updateData)
        // 判断是否已框选
        if(startT > 0L) {
            // 如果未检测，则需要调用yolov7检测
            if(!isDetected) {
                if(detectYolo(bitmap))
                    isDetected = true
                // 如果检测失败则不再处理
                else {
                    // 重置数据
                    reset()
                    runOnUiThread {
                        ttsManager.speakText("检测失败")
                    }
                    return@run false
                }
            }
            else {
                // 判断是否不再需要显示yolo
                if(System.currentTimeMillis() - startT >= MAX_YOLO_DISPLAY_TIME) {
                    startT = 0L
                    isDetected = false
                    // 这个时候也算是消费事件结束了
                    return@run false
                }
                // 绘制yolo
                ImageUtil.markYolo(bitmap, rectF!!, label)
            }
            return@run true
        }
        if(!isNotCancel) {
            App.instance.activity!!.runOnUiThread {
                reset()
                CommonUtils.showToast("cancel!")
                runOnUiThread {
                    ttsManager.speakText("取消操作")
                }
            }
            return@run false
        }
        val result = updateData.result
        when(AbstractHandDetectManager.computeHandGesture(updateData.result.angles)) {
            AbstractHandDetectManager.GESTURE_GUN -> {
                rectF = buildRectF()
                // 记录选中的时间
                startT= System.currentTimeMillis()
                // 需要检测
                isDetected = false
            }
        }
        // 更新指针坐标
        if(result.points.isNotEmpty()) {
            pointX = result.points[8].x
            pointY = result.points[8].y
        }
        // 绘制
        ImageUtil.drawRect(updateData.bitmap, buildRectF())
        return@run true
    }

    private fun buildRectF() = Context.run {
        RectF(
            if(beginX <= pointX) beginX else pointX,
            if(beginY <= pointY) beginY else pointY,
            if(beginX >= pointX) beginX else pointX,
            if(beginY >= pointY) beginY else pointY
        )
    }

    private fun detectYolo(bitmap: Bitmap) = Context.run {
        // 验证rectF是否合法
        val crop: Bitmap = ImageUtil.createBitmap(bitmap, rectF!!) ?: return@run false
        val yoloResult = detectManager.detect(crop)
        if(yoloResult.isNotEmpty()) {
            val center = Vector2(.5f, .5f)
            yoloResult.sortedBy { yoloObject ->
                val yoloCenter = Vector2(
                    yoloObject .rect.right - yoloObject.rect.left ,
                    yoloObject.rect.bottom - yoloObject.rect.top
                )
                sqrt((yoloCenter.x - center.x).pow(2) +
                        (yoloCenter.y - center.y).pow(2))
            }

            label = AbstractYoloDetectManager.labelMap[yoloResult[0].label]
            val rf = yoloResult[0].rect
            // 更新rectF
            rectF?.let {
                val width = bitmap.width
                val height= bitmap.height
                val x = it.left * width
                val y = it.top * height
                rf.left = (rf.left + x) / width
                rf.right= (rf.right + x) / width
                rf.top  = (rf.top + y) / height
                rf.bottom = (rf.bottom + y) / height
                // 更新rectF
                ImageUtil.copyRectF(it, rf)
                // 绘制yolo
                ImageUtil.markYolo(bitmap, it, label)
                // 朗读
                runOnUiThread {
                    if(!ttsManager.speakText(label))
                        Log.e("YoloSelect", "speak failed!!")
                }
                return@run true
            }
        }
        return@run false
    }
}