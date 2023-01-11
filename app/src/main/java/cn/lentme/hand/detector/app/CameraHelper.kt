package cn.lentme.hand.detector.app

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import cn.lentme.hand.detector.util.ImageUtil
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraHelper(
    private val activity: AppCompatActivity,
    private val cameraSelector: CameraSelector,
    private val processor: Processor
    ) {

    // camera处理线程
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var bitmapBuffer: Bitmap

    interface Processor {
        fun process(bitmap: Bitmap)
    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener({
            // 摄像头提供
            val cameraProvider = cameraProviderFuture.get()
            val imageAnalysis = buildImageAnalysis()
            imageAnalysis.setAnalyzer(cameraExecutor) {
                // 获取图片
                if(!::bitmapBuffer.isInitialized) {
                    bitmapBuffer = Bitmap.createBitmap(it.width, it.height,
                        Bitmap.Config.ARGB_8888)
                }
                it.use { image -> bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
                // 翻转图片
                val bitmap = when(cameraSelector) {
                    CameraSelector.DEFAULT_FRONT_CAMERA -> ImageUtil.createRotateBitmap(bitmapBuffer, -90f, true)
                    CameraSelector.DEFAULT_BACK_CAMERA  -> ImageUtil.createRotateBitmap(bitmapBuffer, 90f)
                    else -> ImageUtil.createRotateBitmap(bitmapBuffer, 0f)
                }
                // 处理图片
                processor.process(bitmap)
            }
            try {
                cameraProvider.unbindAll()
                // 绑定生命周期
                cameraProvider.bindToLifecycle(activity, cameraSelector, imageAnalysis)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(activity))
    }

    fun shutDown() {
        cameraExecutor.shutdown()
    }

    private fun buildImageAnalysis() = ImageAnalysis.Builder()
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setImageQueueDepth(1)
        .build()

    companion object {
        private val REQUEST_PERMISIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
    }
}