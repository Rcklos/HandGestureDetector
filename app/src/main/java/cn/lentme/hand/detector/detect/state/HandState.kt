package cn.lentme.hand.detector.detect.state

import android.graphics.Bitmap
import android.util.Log
import cn.lentme.hand.detector.detect.AbstractHandDetectManager
import cn.lentme.hand.detector.entity.HandDetectResult

class HandState {

    private inner class StateContext {
        // 状态
        var state: Int = STATE_IDLE
        // 手势事件
        var event = ""
        // 手势映射
        var eventMap = HashMap<String, StateChangeListener>()
        // 触发手势事件的时间
        var eventTriggerTime = 0L
        // 触发的手势
        var eventTriggerWaitTo  = AbstractHandDetectManager.GESTURE_NONE
        // 触发手势刷新的时间
        var refreshEventTime = 0L
    }

    private val stateContext = StateContext()

    private fun refreshContext() = stateContext.apply {
        state = STATE_IDLE
        eventMap.clear()

        eventTriggerTime = 0L
        eventTriggerWaitTo = AbstractHandDetectManager.GESTURE_NONE
        refreshEventTime = 0L
    }

    init {
        refreshContext()
    }

    fun registerEvent(gesture: String, listener: StateChangeListener) {
        stateContext.apply {
            eventMap[gesture] = listener
        }
    }

    fun process(updateData: UpdateData) {
        when(stateContext.state) {
            STATE_IDLE -> processIdle(updateData)
            STATE_CONSUMING -> processConsume(updateData)
            else -> {
                Log.e("HandState" , "Process error cause shifted to wrong state.")
            }
        }
    }

    private fun processConsume(updateData: UpdateData) = stateContext.apply {
        if(!eventMap[event]!!.consume(updateData)) {
            event = ""
            state = STATE_IDLE
        }
    }

    private fun processIdle(updateData: UpdateData) = stateContext.apply {
        val bitmap  = updateData.bitmap
        val result  = updateData.result
        val angles  = result.angles
        val gesture = AbstractHandDetectManager.computeHandGesture(angles)

        if(eventTriggerWaitTo == AbstractHandDetectManager.GESTURE_NONE) {
            if(gesture != AbstractHandDetectManager.GESTURE_NONE && gesture in eventMap) {
                eventTriggerWaitTo = gesture
                eventTriggerTime = System.currentTimeMillis()
                refreshEventTime = 0L
            }
            return@apply
        }

        // 更新等待的姿势
        val now = System.currentTimeMillis()
        if(gesture == eventTriggerWaitTo) {
            refreshEventTime = 0L
            val duration = now - eventTriggerTime;
            val listener = eventMap[gesture]
            // 变化完成的后会重置上下文的内容并切换状态
            if(listener!!.changed(duration, updateData)) {
                event = eventTriggerWaitTo
                eventTriggerWaitTo = AbstractHandDetectManager.GESTURE_NONE
                eventTriggerTime = 0L
                refreshEventTime = 0L
                state = STATE_CONSUMING
            }
        }
        else if(refreshEventTime != 0L){
            // 刷新计时结束会切换新的姿势状态或者返回IDLE
            if(now - refreshEventTime >= REFRESH_EVENT_WAIT_REFERENCE) {
                if(gesture != AbstractHandDetectManager.GESTURE_NONE &&
                        gesture in eventMap
                ) {
                    eventTriggerWaitTo = gesture
                    eventTriggerTime = refreshEventTime
                }
                else {
                    eventTriggerWaitTo = AbstractHandDetectManager.GESTURE_NONE
                    eventTriggerTime = 0L
                }
                refreshEventTime = 0L
            }
        }
        else {
            // 姿势不一样则开始刷新计时
            refreshEventTime = now
        }
    }

    data class UpdateData(
        val bitmap: Bitmap,
        val result: HandDetectResult
    )

    companion object {
        // 毫秒
        private const val TRIGGER_EVENT_TIME_REFERENCE  = 3000L
        // 等待触发某个姿势事件时，若当前姿势不同于等待的姿势超过以下规定的时间
        // 则会自动刷新，并将时间叠加给等待的新姿势
        private const val REFRESH_EVENT_WAIT_REFERENCE  = 100L
        private const val STATE_IDLE            = 0
        private const val STATE_CONSUMING       = 1
    }
}