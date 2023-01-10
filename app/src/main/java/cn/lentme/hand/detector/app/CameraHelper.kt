package cn.lentme.hand.detector.app

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraHelper(
    private val activity: AppCompatActivity,
    private val cameraSelector: CameraSelector,
    private val analyzer: Analyzer = Analyzer {  }
    ) {

    // camera处理线程
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener({
            // 摄像头提供
            val cameraProvider = cameraProviderFuture.get()
            val imageAnalysis = buildImageAnalysis()
            imageAnalysis.setAnalyzer(cameraExecutor, analyzer)
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