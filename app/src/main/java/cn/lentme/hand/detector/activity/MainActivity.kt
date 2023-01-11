package cn.lentme.hand.detector.activity

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.camera.core.CameraSelector
import cn.lentme.hand.detector.app.CameraHelper
import cn.lentme.hand.detector.databinding.ActivityMainBinding
import cn.lentme.hand.detector.detect.AbstractHandDetectManager
import cn.lentme.hand.detector.detect.state.HandState
import cn.lentme.hand.detector.detect.state.listener.YoloSelectBoxStateChangeListener
import cn.lentme.hand.detector.request.viewmodel.MainViewModel
import cn.lentme.mvvm.base.BaseActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity: BaseActivity<ActivityMainBinding, MainViewModel>() {
    override fun fetchBinding() = ActivityMainBinding.inflate(layoutInflater)
    override fun fetchViewModel(): MainViewModel = getViewModel()

    private lateinit var cameraHelper: CameraHelper

    private val handState: HandState by inject()

    override fun initData() {
        initCamera()

        // 注册手势事件
        handState.registerEvent(AbstractHandDetectManager.GESTURE_ONE,
            YoloSelectBoxStateChangeListener())
    }

    private fun initCamera() {
        cameraHelper = CameraHelper(
            this, CameraSelector.DEFAULT_FRONT_CAMERA, buildProcessor()
        )
        // Request camera permissions
        if (allPermissionsGranted()) {
            cameraHelper.startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS) {
                if (allPermissionsGranted()) {
                    cameraHelper.startCamera()
                } else {
                    Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun initUI() {
        mViewModel.cropBitmap.observe(this) {
            it?.let {
                mBinding.mainSelected.setImageBitmap(it)
            }
        }
    }

    private fun buildProcessor() = object: CameraHelper.Processor {
        override fun process(bitmap: Bitmap) {
            // 手部检测
            val result = mViewModel.detectHand(bitmap)
            val resultBitmap = result.bitmap

            // 状态机
            handState.process(HandState.UpdateData(
                resultBitmap,
                result
            ))

            // 更新视图
            runOnUiThread {
                mBinding.mainSurface.setImageBitmap(resultBitmap)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraHelper.shutDown()
    }

    private fun allPermissionsGranted() =
        isPermissionGranted(REQUIRED_PERMISSIONS)

    companion object {
        private const val TAG = "CameraMainActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
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