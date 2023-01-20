package cn.lentme.hand.detector.activity

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import cn.lentme.hand.detector.camera.CameraHelper
import cn.lentme.hand.detector.camera.CameraLifecycle
import cn.lentme.hand.detector.databinding.ActivityMainBinding
import cn.lentme.hand.detector.detect.AbstractHandDetectManager
import cn.lentme.hand.detector.detect.state.HandState
import cn.lentme.hand.detector.detect.state.listener.ChangeToRenderStateChangeListener
import cn.lentme.hand.detector.detect.state.listener.YoloSelectBoxStateChangeListener
import cn.lentme.hand.detector.request.viewmodel.MainViewModel
import cn.lentme.mvvm.base.BaseActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity: BaseActivity<ActivityMainBinding, MainViewModel>() {
    override fun fetchBinding() = ActivityMainBinding.inflate(layoutInflater)
    override fun fetchViewModel(): MainViewModel = getViewModel()

    private lateinit var cameraHelper: CameraHelper
    private lateinit var lifecycle: CameraLifecycle

    private val handState: HandState = HandState()

    override fun initData() {
        initCamera()

        // 注册手势事件
        handState.registerEvent(AbstractHandDetectManager.GESTURE_ONE,
            YoloSelectBoxStateChangeListener())

        // 注册切换界面的手势
        handState.registerEvent(AbstractHandDetectManager.GESTURE_SIX,
            ChangeToRenderStateChangeListener())
    }

    private fun initCamera() {
        lifecycle = CameraLifecycle()
        cameraHelper = CameraHelper(
            lifecycle, CameraSelector.DEFAULT_BACK_CAMERA, buildProcessor()
        )
    }

    override fun onResume() {
        super.onResume()
        lifecycle.cameraOnCreate()
        lifecycle.cameraOnStart()
        lifecycle.cameraOnResume()
        // Request camera permissions
        if (allPermissionsGranted()) {
            cameraHelper.bindCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS) {
                if (allPermissionsGranted()) {
                    cameraHelper.bindCamera()
                } else {
                    Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycle.cameraOnPause()
        lifecycle.cameraOnStop()
        lifecycle.cameraOnDestroyed()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraHelper.shutDown()
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