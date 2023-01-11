package cn.lentme.hand.detector.detect.state

interface StateChangeListener {
    /**
     * 状态变化中，idle转变为consuming期间的回调
     * @return 如果变化结束则返回true, 如果还未确认状态，则返回false
     */
    fun changed(duration: Long, updateData: HandState.UpdateData): Boolean

    /**
     * 状态完成变化，此时state已经变成了consuming的回调
     * 只要consume一直返回true，那么就不会改变状态
     * 返回false的时候会将状态变回idle
     */
    fun consume(updateData: HandState.UpdateData): Boolean
}
