package cn.lentme.hand.detector.request.repository

import android.graphics.Canvas
import android.graphics.RectF
import android.util.Log
import android.util.Size
import cn.lentme.hand.detector.entity.HandDetectResult
import cn.lentme.hand.detector.util.ImageUtil
import cn.lentme.mvvm.base.BaseRepository

class HandSelectorRepository: BaseRepository() {
    private companion object {
        const val TAG = "MainStatus"
        const val Idle = 0
        const val Select = 1
        const val Confirm = 2
    }

    private var status = Idle
    private var recordGesture = "None"

    private object Record {
        const val UPDATE_TIME_REFERENCE = 20
        var gesture = ""
        var updateCount = 0
    }

    private object UpdateTimer {
        const val REFRESH_TIME_REFERENCE = 33L      // 30fps
        var count = 0L
        var updateTime = 0L
        var lastUpdateTime = 0
        var lastRefreshTime = System.currentTimeMillis()
    }

    private object SelectStatus {
        const val SELECT_COMPLETE_REFERENCE = 1000L
        var beginMillis = 0L
        var beginX = 0f
        var beginY = 0f
        var selected = false
        var x = 0f
        var y = 0f
        var angle = 0f
    }

    private fun resetSelectStatus() {
        SelectStatus.beginMillis = 0L
        SelectStatus.beginX     = 0f
        SelectStatus.beginY     = 0f
        SelectStatus.x          = 0f
        SelectStatus.y          = 0f
        SelectStatus.angle      = 0f
        SelectStatus.selected   = false
    }

    private fun resetUpdateTimer() {
        UpdateTimer.updateTime = 0L
        UpdateTimer.lastUpdateTime = 0
        UpdateTimer.count = 0
        UpdateTimer.lastRefreshTime = System.currentTimeMillis()
    }

    private fun confirmSelect(): RectF? {
        if(!SelectStatus.selected) return null
        val left = if(SelectStatus.beginX < SelectStatus.x)
            SelectStatus.beginX else SelectStatus.x
        val right = if(SelectStatus.beginX > SelectStatus.x)
            SelectStatus.beginX else SelectStatus.x
        val top = if(SelectStatus.beginY < SelectStatus.y)
            SelectStatus.beginY else SelectStatus.y
        val bottom = if(SelectStatus.beginY > SelectStatus.y)
            SelectStatus.beginY else SelectStatus.y
        return RectF(left, top, right, bottom)
    }

    private fun isReadyToUpdate(): Boolean {
        val nowMillis = System.currentTimeMillis()
        if(nowMillis - UpdateTimer.lastRefreshTime < UpdateTimer.REFRESH_TIME_REFERENCE)
            return false
        UpdateTimer.lastRefreshTime = nowMillis
        return true
    }

    private fun setRecord(gesture: String) {
        Record.gesture = gesture
        Record.updateCount = 0
    }

    private fun keepGesture(gesture: String) {
        if(Record.gesture == "") setRecord(gesture)
        if(Record.updateCount++ <= Record.UPDATE_TIME_REFERENCE)
            if(Record.gesture == gesture) Record.updateCount = 0
        else setRecord(gesture)
    }

    private fun isExpectGesture(gesture: String, failedFunction: () -> Unit,
                                acceptFunction: () -> Unit): Boolean {
        if(Record.gesture == gesture) {
            acceptFunction()
            return true
        }
        failedFunction()
        return false
    }

    private fun isNotExpectGesture(gesture: String, failedFunction: () -> Unit,
                                   acceptFunction: () -> Unit): Boolean {
        if(Record.gesture != gesture) {
            acceptFunction()
            return true
        }
        failedFunction()
        return false
    }

    private fun updateIdleStatus(canvas: Canvas, screenSize: Size, result: HandDetectResult) {
        isExpectGesture("一", { resetSelectStatus() }) {
            if(SelectStatus.beginMillis == 0L && result.points.isNotEmpty())
                SelectStatus.beginMillis = System.currentTimeMillis()
            val delay = System.currentTimeMillis() - SelectStatus.beginMillis
            SelectStatus.angle = (delay.toFloat() / SelectStatus.SELECT_COMPLETE_REFERENCE) * 360
            if(SelectStatus.angle > 360) SelectStatus.angle = 360f
//        Log.d(TAG, "select angle ---> ${SelectStatus.angle}")
            if(result.points.isNotEmpty()) {
                SelectStatus.x = result.points[8].x
                SelectStatus.y = result.points[8].y
            }
            // 绘制
            ImageUtil.drawArc(canvas, screenSize,
                SelectStatus.x, SelectStatus.y, 0.08f, SelectStatus.angle)
            if(SelectStatus.angle == 360f) status = Select
        }
    }

    private fun updateSelectStatus(canvas: Canvas, screenSize: Size,
                                   result: HandDetectResult): RectF?{
        var resultRectF: RectF? = null
        isNotExpectGesture("枪", {
            Log.d(TAG, "confirm select!!!!!!")
            resultRectF = confirmSelect()
            resetUpdateTimer()
            status = Idle
        }) {
            // 实现逻辑
            if(!SelectStatus.selected && result.points.isNotEmpty()) {
                SelectStatus.selected = true
                SelectStatus.beginX = result.points[8].x
                SelectStatus.beginY = result.points[8].y
            }
            if(!SelectStatus.selected) return@isNotExpectGesture
            if(result.points.isNotEmpty()) {
                SelectStatus.x = result.points[8].x
                SelectStatus.y = result.points[8].y
            }
            val left = if(SelectStatus.beginX < SelectStatus.x)
                SelectStatus.beginX else SelectStatus.x
            val right = if(SelectStatus.beginX > SelectStatus.x)
                SelectStatus.beginX else SelectStatus.x
            val top = if(SelectStatus.beginY < SelectStatus.y)
                SelectStatus.beginY else SelectStatus.y
            val bottom = if(SelectStatus.beginY > SelectStatus.y)
                SelectStatus.beginY else SelectStatus.y
            val rectF = RectF(left, top, right, bottom)
            ImageUtil.drawRect(canvas, screenSize, rectF)
        }
        return resultRectF
    }

    fun updateHandSelector(canvas: Canvas, screenSize: Size,
                           result: HandDetectResult, gesture: String): RectF? {
        // realGesture
        keepGesture(gesture)
        // 控制update频率
        if(!isReadyToUpdate()) return null
//        Log.d(TAG, "status ----> $status")
        return when(status) {
            Idle -> {
                updateIdleStatus(canvas, screenSize, result)
                null
            }
            Select -> {
                updateSelectStatus(canvas, screenSize, result)
            }
            else -> null
        }
    }
}