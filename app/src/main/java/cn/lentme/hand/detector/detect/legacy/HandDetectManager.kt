package cn.lentme.hand.detector.detect.legacy

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import cn.lentme.hand.detector.entity.HandDetectResult
import cn.lentme.hand.detector.entity.Vector2
import cn.lentme.hand.detector.detect.AbstractHandDetectManager
import cn.lentme.mediapipe.handlandmark.HandDetector
import cn.lentme.mediapipe.handlandmark.data.HandLandmark

class HandDetectManager(context: Context): AbstractHandDetectManager() {
    private lateinit var detector: HandDetector

    init {
        detector = HandDetector.create(context)
    }

    override fun detectHand(bitmap: Bitmap): HandDetectResult {
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