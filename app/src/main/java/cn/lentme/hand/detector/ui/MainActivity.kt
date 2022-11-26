package cn.lentme.hand.detector.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.view.Surface
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.Preview
import androidx.camera.core.impl.ImageAnalysisConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cn.lentme.hand.detector.app.App
import cn.lentme.hand.detector.app.HandDetectManager
import cn.lentme.hand.detector.databinding.ActivityMainBinding
import cn.lentme.hand.detector.request.viewmodel.MainViewModel
import cn.lentme.mvvm.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity: BaseActivity<ActivityMainBinding, MainViewModel>() {
    override fun fetchBinding() = ActivityMainBinding.inflate(layoutInflater)
    override fun fetchViewModel(): MainViewModel = getViewModel()

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var bitmapBuffer: Bitmap
    private lateinit var handDetectManager: HandDetectManager

    override fun initData() {
        handDetectManager = HandDetectManager(this)
        cameraExecutor = Executors.newSingleThreadExecutor()
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val imageAnalysis = ImageAnalysis.Builder()
                .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .setTargetRotation(Surface.ROTATION_90)
                .setImageQueueDepth(1)
                .build()
            imageAnalysis.setAnalyzer(cameraExecutor) {
                if(!::bitmapBuffer.isInitialized) {
                    bitmapBuffer = Bitmap.createBitmap(it.width, it.height,
                        Bitmap.Config.ARGB_8888)
                }
                it.use { image -> bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
                val matrix = Matrix()
                matrix.setRotate(90f)
                val bitmap = Bitmap.createBitmap(bitmapBuffer, 0, 0,
                    bitmapBuffer.width, bitmapBuffer.height, matrix, true)

                val handDetectResult = handDetectManager.detectAndDraw(bitmap)
                val resultBitmap = handDetectResult.bitmap
                val angles = handDetectResult.angles

                runOnUiThread {
                    mViewModel.gesture.value =
                        handDetectManager.computeHandGesture(angles)
                    mBinding.mainSurface.setImageBitmap(resultBitmap)
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(this,
//                    cameraSelector, preview, imageAnalysis)
                cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun initUI() {
        mViewModel.gesture.observe(this) { title = it }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}