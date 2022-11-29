package cn.lentme.hand.detector.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.BoringLayout
import cn.lentme.hand.detector.entity.HandDetectResult
import cn.lentme.hand.detector.entity.Vector2
import cn.lentme.mediapipe.handlandmark.HandDetector
import cn.lentme.mediapipe.handlandmark.data.HandLandmark
import kotlin.math.acos
import kotlin.math.sqrt

class HandDetectManager(context: Context) {
    private lateinit var detector: HandDetector

    init {
        detector = HandDetector.create(context)
    }

    fun detect(bitmap: Bitmap): List<Double> {
        return ArrayList()
    }

    fun detectAndDraw(bitmap: Bitmap): HandDetectResult {
        val landmark = detector.process(bitmap)
        if (landmark.isEmpty()) return HandDetectResult(bitmap, ArrayList(), ArrayList())
        val resultBitmap = showLandmarks(bitmap, landmark)
        val angles = computeHandAngle(landmark)
        val points = ArrayList<Vector2>()
        landmark.forEach {
            points.add(Vector2(it.x, it.y))
        }
        return HandDetectResult(resultBitmap, angles, points)
    }

    private fun computeHandAngle(landmarks: List<HandLandmark>): List<Double> {
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

    private inline fun isThumbBent(angle: Double): Boolean = isBent(angle, 53.0)
    private inline fun isBent(angle: Double): Boolean = isBent(angle, 65.0)
    private inline fun isStraight(angle: Double): Boolean = !isBent(angle, 49.0)

    private inline fun isBent(angle: Double, bent: Double): Boolean {
        return angle > bent
    }

    fun computeHandGesture(angles: List<Double>): String =
        if (angles.isEmpty()) "None"
        else if (isThumbBent(angles[0]) && isBent(angles[1]) &&
            isBent(angles[2]) && isBent(angles[3]) && isBent(angles[4]))
            "拳头"
        else if (isThumbBent(angles[0]) && isStraight(angles[1]) &&
            isBent(angles[2]) && isBent(angles[3]) && isBent(angles[4]))
            "一"
        else if (isThumbBent(angles[0]) && isStraight(angles[1]) &&
            isStraight(angles[2]) && isBent(angles[3]) && isBent(angles[4]))
            "二"
        else if (isThumbBent(angles[0]) && isStraight(angles[1]) &&
            isStraight(angles[2]) && isStraight(angles[3]) && isBent(angles[4]))
            "三"
        else if (isThumbBent(angles[0]) && isStraight(angles[1]) &&
            isStraight(angles[2]) && isStraight(angles[3]) && isStraight(angles[4]))
            "四"
        else if (isStraight(angles[0]) && isStraight(angles[1]) &&
            isStraight(angles[2]) && isStraight(angles[3]) && isStraight(angles[4]))
            "五"
        else if(isStraight(angles[0]) && isBent(angles[1]) && isBent(angles[2]) &&
            isBent(angles[3]) && isStraight(angles[4]))
            "六"
        else if(isThumbBent(angles[0]) && isBent(angles[1]) && isStraight(angles[2]) &&
                isBent(angles[3]) && isBent(angles[4]))
            "友好手势"
        else if (isStraight(angles[0]) && isStraight(angles[1]) &&
            isBent(angles[2]) && isBent(angles[3]) && isBent(angles[4]))
            "枪"
        else
            "Other"

    private fun computeVectorAngle(v1x: Float, v1y: Float, v2x: Float, v2y: Float): Double {
        try {
            val x = (v1x * v2x + v1y * v2y) /
                    (sqrt(v1x * v1x + v1y * v1y) * sqrt(v2x * v2x + v2y * v2y)).toDouble()
            val angle = Math.toDegrees(acos(x))
            if(angle > 180) return 65535.0
            return angle
        } catch (e: Exception) {
            return 65535.0
        }
    }

    /**
     * Make a copy of the input image and draw hand landmarks on top ut.
     */
    private fun showLandmarks(inputImage: Bitmap, landmarks: List<HandLandmark>): Bitmap {
        val drawBitmap = inputImage.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(drawBitmap)
        val penStroke = Paint().apply {
            color = Color.GREEN
            strokeWidth = 5f
        }
        val penPoint = Paint().apply {
            color = Color.RED
            strokeWidth = 2f
            style = Paint.Style.FILL
        }
        val lines = mutableListOf<Float>()
        val points = mutableListOf<Float>()
        for (i in landmarkConnections.indices step 2) {
            val startX =
                landmarks[landmarkConnections[i]].x * drawBitmap.width
            val startY =
                landmarks[landmarkConnections[i]].y * drawBitmap.height
            val endX =
                landmarks[landmarkConnections[i + 1]].x * drawBitmap.width
            val endY =
                landmarks[landmarkConnections[i + 1]].y * drawBitmap.height

            lines.add(startX)
            lines.add(startY)
            lines.add(endX)
            lines.add(endY)
            points.add(startX)
            points.add(startY)
        }
        canvas.drawLines(lines.toFloatArray(), penStroke)
        canvas.drawPoints(points.toFloatArray(), penPoint)
        return drawBitmap
    }

    fun close() {
        detector.close()
    }

    companion object {
//        private const val TAG = "Hand detection"
//        private const val REQUEST_IMAGE_CAPTURE = 1

        // This list defines the lines that are drawn when visualizing the hand landmark detection
        // results. These lines connect:
        // landmarkConnections[2*n] and landmarkConnections[2*n+1]
        private val landmarkConnections = listOf(
            HandLandmark.WRIST,
            HandLandmark.THUMB_CMC,
            HandLandmark.THUMB_CMC,
            HandLandmark.THUMB_MCP,
            HandLandmark.THUMB_MCP,
            HandLandmark.THUMB_IP,
            HandLandmark.THUMB_IP,
            HandLandmark.THUMB_TIP,
            HandLandmark.WRIST,
            HandLandmark.INDEX_FINGER_MCP,
            HandLandmark.INDEX_FINGER_MCP,
            HandLandmark.INDEX_FINGER_PIP,
            HandLandmark.INDEX_FINGER_PIP,
            HandLandmark.INDEX_FINGER_DIP,
            HandLandmark.INDEX_FINGER_DIP,
            HandLandmark.INDEX_FINGER_TIP,
            HandLandmark.INDEX_FINGER_MCP,
            HandLandmark.MIDDLE_FINGER_MCP,
            HandLandmark.MIDDLE_FINGER_MCP,
            HandLandmark.MIDDLE_FINGER_PIP,
            HandLandmark.MIDDLE_FINGER_PIP,
            HandLandmark.MIDDLE_FINGER_DIP,
            HandLandmark.MIDDLE_FINGER_DIP,
            HandLandmark.MIDDLE_FINGER_TIP,
            HandLandmark.MIDDLE_FINGER_MCP,
            HandLandmark.RING_FINGER_MCP,
            HandLandmark.RING_FINGER_MCP,
            HandLandmark.RING_FINGER_PIP,
            HandLandmark.RING_FINGER_PIP,
            HandLandmark.RING_FINGER_DIP,
            HandLandmark.RING_FINGER_DIP,
            HandLandmark.RING_FINGER_TIP,
            HandLandmark.RING_FINGER_MCP,
            HandLandmark.PINKY_MCP,
            HandLandmark.WRIST,
            HandLandmark.PINKY_MCP,
            HandLandmark.PINKY_MCP,
            HandLandmark.PINKY_PIP,
            HandLandmark.PINKY_PIP,
            HandLandmark.PINKY_DIP,
            HandLandmark.PINKY_DIP,
            HandLandmark.PINKY_TIP
        )
    }
}