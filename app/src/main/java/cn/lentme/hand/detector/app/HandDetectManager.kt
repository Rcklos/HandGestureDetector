package cn.lentme.hand.detector.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import cn.lentme.mediapipe.handlandmark.HandDetector
import cn.lentme.mediapipe.handlandmark.data.HandLandmark

class HandDetectManager(context: Context) {
    private lateinit var detector: HandDetector

    init {
        detector = HandDetector.create(context)
    }

    private fun detect(bitmap: Bitmap): List<HandLandmark> {
        return detector.process(bitmap)
    }

    fun detectAndDraw(bitmap: Bitmap): Bitmap {
        val landmark = detect(bitmap)
        if (landmark == null || landmark.isEmpty()) return bitmap
        return showLandmarks(bitmap, landmark)
    }

    /**
     * Make a copy of the input image and draw hand landmarks on top ut.
     */
    private fun showLandmarks(inputImage: Bitmap, landmarks: List<HandLandmark>): Bitmap {
        val drawBitmap = inputImage.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(drawBitmap)
        val penStroke = Paint().apply {
            color = Color.GREEN
            strokeWidth = 20f
        }
        val penPoint = Paint().apply {
            color = Color.RED
            strokeWidth = 20f
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

    private companion object {
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