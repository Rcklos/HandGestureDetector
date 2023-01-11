package cn.lentme.hand.detector.detect.state

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Size
import androidx.annotation.CallSuper
import cn.lentme.hand.detector.app.App
import cn.lentme.hand.detector.util.CommonUtils
import cn.lentme.hand.detector.util.ImageUtil

open class DefaultStateChangeListener: StateChangeListener {
    protected fun drawArc(bitmap: Bitmap, x: Float, y: Float, radius: Float, degree: Float) {
        ImageUtil.drawArc(
            Canvas(bitmap),
            Size(bitmap.width, bitmap.height),
            x, y, radius, degree
        )
    }

    private object Context {
        var cancelTriggerTime = 0L
    }

    override fun changed(duration: Long, updateData: HandState.UpdateData): Boolean {
        val bitmap = updateData.bitmap
        if(duration > 0L) {
            drawArc(bitmap, .5f, .5f, .15f,
                duration.toFloat() / TRIGGER_EVENT_TIME_REFERENCE * 360)
        }
        return duration >= TRIGGER_EVENT_TIME_REFERENCE
    }

    override fun consume(updateData: HandState.UpdateData): Boolean = Context.run {
        if(updateData.result.points.isEmpty()) {
            if(cancelTriggerTime == 0L) cancelTriggerTime = System.currentTimeMillis()
            return@run System.currentTimeMillis() - cancelTriggerTime >= CANCEL_EVENT_TIME_REFERENCE
        }
        else cancelTriggerTime = 0L
        return@run true
    }

    protected fun runOnUiThread(runnable: Runnable) {
        App.instance.activity!!.runOnUiThread(runnable)
    }

    companion object {
        const val TRIGGER_EVENT_TIME_REFERENCE  = 3000L
        const val CANCEL_EVENT_TIME_REFERENCE   = 1000L
    }
}