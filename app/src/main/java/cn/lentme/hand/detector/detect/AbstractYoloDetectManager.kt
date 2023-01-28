package cn.lentme.hand.detector.detect

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import cn.lentme.hand.detector.entity.YoloDetectResult

abstract class AbstractYoloDetectManager() {
    abstract fun detect(bitmap: Bitmap): List<YoloDetectResult>

    companion object {
        val labelMap = listOf(
            "行人", "自行车", "车辆", "摩托车", "飞机", "公交车", "列车", "卡车", "船艇",
            "红绿灯",
            "消防栓", "停止标", "停车计时器", "长凳", "小鸟", "猫咪", "狗狗", "马儿",
            "羊儿", "牛牛",
            "大象", "熊", "斑马", "长颈鹿", "背包", "雨伞", "小包包", "领带",
            "手提箱", "飞盘",
            "滑板鞋", "滑雪板", "球", "风筝", "羽毛球拍", "棒球棍",
            "滑板", "冲浪板",
            "tennis racket", "瓶子", "酒杯", "被子", "fork", "小刀", "spoon", "bowl",
            "香蕉", "苹果",
            "sandwich", "橘子", "broccoli", "carrot", "热狗", "披萨", "donut", "蛋糕",
            "椅子", "couch",
            "potted plant", "床", "餐桌", "toilet", "屏幕", "笔记本", "鼠标", "remote",
            "键盘", "手机",
            "微波炉", "oven", "toaster", "sink", "冰箱", "书", "闹钟", "花瓶",
            "剪刀", "teddy bear",
            "吹风机", "牙刷"
        )
    }
}