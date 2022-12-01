package cn.lentme.hand.detector.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cn.lentme.hand.detector.databinding.ActivityMainBinding
import cn.lentme.hand.detector.request.viewmodel.MainViewModel
import cn.lentme.hand.detector.utils.ImageUtil
import cn.lentme.mvvm.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity: BaseActivity<ActivityMainBinding, MainViewModel>() {
    override fun fetchBinding() = ActivityMainBinding.inflate(layoutInflater)
    override fun fetchViewModel(): MainViewModel = getViewModel()

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var bitmapBuffer: Bitmap


    override fun initData() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun initUI() {
        mViewModel.gesture.observe(this) { title = it }
        mViewModel.selected.observe(this) {
            it?.let {
                mBinding.mainSelected.setImageBitmap(it)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val imageAnalysis = buildImageAnalysis()
            imageAnalysis.setAnalyzer(cameraExecutor, buildImageAnalyzer())
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun buildImageAnalysis() = ImageAnalysis.Builder()
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setImageQueueDepth(1)
        .build()

    private fun buildImageAnalyzer() = ImageAnalysis.Analyzer {
        if(!::bitmapBuffer.isInitialized) {
            bitmapBuffer = Bitmap.createBitmap(it.width, it.height,
                Bitmap.Config.ARGB_8888)
        }
        it.use { image -> bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
        val bitmap = ImageUtil.createRotateBitmap(bitmapBuffer, 90f)
        val result = mViewModel.detectHandAndDraw(bitmap)
        val resultBitmap = result.bitmap
        val angles = result.angles
//        val points = result.points
        val gesture = mViewModel.computeHandGesture(angles)

        // 手势结果判断
//        if (gesture == "一") {
//            Log.d(TAG, "gesture ===> 1")
//            ImageUtil.drawArc(resultBitmap, points[8].x, points[8].y, 0.05f, 210f)
//        }
        val rectF = mViewModel.updateHandSelector(resultBitmap, result, gesture)
        var cropBitmap: Bitmap? = null
        rectF?.let { _ ->
            cropBitmap = ImageUtil.createBitmap(bitmap, rectF)
        }

        runOnUiThread {
            cropBitmap?.let { bmp -> mViewModel.selected.value = bmp }
            mViewModel.gesture.value = gesture
            mBinding.mainSurface.setImageBitmap(resultBitmap)
        }
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
        private const val TAG = "CameraMainActivity"
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