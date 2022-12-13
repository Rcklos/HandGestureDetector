package cn.lentme.hand.detector.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cn.lentme.hand.detector.databinding.ActivityMainBinding
import cn.lentme.hand.detector.detect.AbstractYoloDetectManager
import cn.lentme.hand.detector.entity.Vector2
import cn.lentme.hand.detector.request.viewmodel.MainViewModel
import cn.lentme.hand.detector.utils.ImageUtil
import cn.lentme.mvvm.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity: BaseActivity<ActivityMainBinding, MainViewModel>() {
    override fun fetchBinding() = ActivityMainBinding.inflate(layoutInflater)
    override fun fetchViewModel(): MainViewModel = getViewModel()

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var bitmapBuffer: Bitmap

    // yolo
    private val cropRectF = RectF()
    private val yoloRectF = RectF()
    private var labelBuffer = ""
    private var selected = false
    private var speechFlag = false
    private var antiShakeYolo = false
    private var countTime = 0L
    private val MAX_COUNT_TIME = 1000L


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
//        mViewModel.gesture.observe(this) { title = it }
        mViewModel.cropBitmap.observe(this) {
            it?.let {
                mBinding.mainSelected.setImageBitmap(it)
            }
        }

        mViewModel.selected.observe(this) {
            selected = it
            if(it) {
                countTime = System.currentTimeMillis()
                speechFlag = false
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
        // 获取图片
        if(!::bitmapBuffer.isInitialized) {
            bitmapBuffer = Bitmap.createBitmap(it.width, it.height,
                Bitmap.Config.ARGB_8888)
        }
        it.use { image -> bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
        val bitmap = ImageUtil.createRotateBitmap(bitmapBuffer, 90f)

        // 防抖获取Yolo数据
        if(selected) {
            if(antiShakeYolo) computeAndDrawYolo(bitmap)
            else ImageUtil.markYolo(bitmap, yoloRectF, labelBuffer)

            if(System.currentTimeMillis() - countTime >= MAX_COUNT_TIME)
                mViewModel.setSelected(false)

            if(!speechFlag) {
                runOnUiThread {
                    if(!mViewModel.speech(labelBuffer))
                        Log.e(TAG, "speak failed!!!")
                }
                speechFlag = true
            }
        }

        // 计算手
        val result = mViewModel.detectHandAndDraw(bitmap)
        val resultBitmap = result.bitmap
        val angles = result.angles
        val gesture = mViewModel.computeHandGesture(angles)

        val rectF = mViewModel.updateHandSelector(resultBitmap, result, gesture)
        var cropBitmap: Bitmap? = null
        rectF?.let { _ ->
            ImageUtil.logRectF(TAG, "crop-before", cropRectF)
            ImageUtil.copyRectF(cropRectF, rectF)
            ImageUtil.logRectF(TAG, "crop-after", cropRectF)
            mViewModel.setSelected(true)
            cropBitmap = ImageUtil.createBitmap(bitmap, rectF)

            // 如果不防抖直接在这里处理
            if(!antiShakeYolo) computeAndDrawYolo(bitmap)
        }


        runOnUiThread {
            cropBitmap?.let { bmp -> mViewModel.cropBitmap.value = bmp }
            mViewModel.gesture.value = gesture
            mBinding.mainSurface.setImageBitmap(resultBitmap)
        }
    }

    private fun computeAndDrawYolo(bitmap: Bitmap) {
        var crop = ImageUtil.createBitmap(bitmap, cropRectF)
        if(crop == null) {
            mViewModel.setSelected(false)
            return
        }
        val yoloResult = mViewModel.detectYolo(crop)
        if(yoloResult.isNotEmpty()) {
            val center = Vector2(.5f, .5f)
            if(yoloResult.isNotEmpty()) {
                yoloResult.sortedBy { yoloObject ->
                    val yoloCenter = Vector2(
                        yoloObject .rect.right - yoloObject.rect.left ,
                        yoloObject.rect.bottom - yoloObject.rect.top
                    )
                    sqrt((yoloCenter.x - center.x).pow(2) +
                            (yoloCenter.y - center.y).pow(2))
                }

                val label = AbstractYoloDetectManager.labelMap[yoloResult[0].label]
                val rectF = yoloResult[0].rect
                // 更新rect buffer
                val x = cropRectF.left * bitmap.width
                val y = cropRectF.top * bitmap.height
                rectF.left = (rectF.left + x) / bitmap.width
                rectF.right = (rectF.right + x) / bitmap.width
                rectF.top = (rectF.top + y) / bitmap.height
                rectF.bottom = (rectF.bottom + y) / bitmap.height

                crop = ImageUtil.createBitmap(bitmap, rectF)
                runOnUiThread {
                    crop?.let { mViewModel.cropBitmap.value = it }
                }


                labelBuffer = label
                ImageUtil.copyRectF(yoloRectF, rectF)
                ImageUtil.markYolo(bitmap, rectF, label)
            }
            runOnUiThread {
                val msg = if(yoloResult.isEmpty()) "等待检测...." else
                    AbstractYoloDetectManager.labelMap[yoloResult[0].label]
                this.title = msg
//                Toast.makeText(this@MainActivity,
//                    "select: $msg",
//                    Toast.LENGTH_SHORT).show()
            }
            // 没检测到就直接重置选择区域
        } else mViewModel.setSelected(false)

    }



    private fun isRectIntersect(r1: RectF, r2: RectF): Boolean {
        val dx = abs((r1.left + r1.right) / 2 - (r2.left + r2.right) / 2)
        val dy = abs((r1.bottom + r1.top) / 2 - (r2.bottom + r2.top) / 2)
        val hw = (r1.right - r1.left) / 2 + (r2.right - r2.left) / 2
        val hy = (r1.bottom - r1.top) / 2 + (r2.bottom - r2.top) / 2
        return dx <= hw && dy <= hy
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