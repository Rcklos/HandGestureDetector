package cn.lentme.hand.detector.activity

import android.Manifest
import android.app.ActivityManager
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import cn.lentme.gles.render.GL_SIMPLE_CAMERA2
import cn.lentme.gles.render.MyNativeRender
import cn.lentme.gles.render.ui.MyGLSurfaceView
import cn.lentme.hand.detector.databinding.ActivityRenderBinding
import cn.lentme.hand.detector.request.viewmodel.GLRenderViewModel
import cn.lentme.mvvm.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.getViewModel

class GLRenderActivity: BaseActivity<ActivityRenderBinding, GLRenderViewModel>() {
    override fun fetchBinding(): ActivityRenderBinding =
        ActivityRenderBinding.inflate(layoutInflater)

    override fun fetchViewModel(): GLRenderViewModel = getViewModel()

    private lateinit var mRender: MyNativeRender
    private lateinit var mSurface: MyGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        // 初始化OpenGL ES
        initSurface()
    }

    private fun initSurface(type: Int = GL_SIMPLE_CAMERA2) {
        mRender = MyNativeRender(this)
        mSurface = mBinding.surfaceRender
        if(!detectOpenGLES30()) {
            Log.e("MyNativeRender", "OpenGL ES 3.0 not supported on device.  Exiting...")
            finish()
            return
        }

        // 设置OpenGL ES版本
        mSurface.setEGLContextClientVersion(CONTEXT_CLIENT_VERSION)
        // 绑定render
        mSurface.setRenderer(mRender)
        // 设置渲染模式
        mSurface.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        // 设置sample
        mRender.glesSetType(type)
    }

    override fun onResume() {
        super.onResume()
        mSurface.onResume()

        mRender.copyAssetsToCache(this)
    }

    private fun detectOpenGLES30(): Boolean {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val info = am.deviceConfigurationInfo
        return info.reqGlEsVersion >= 0x30000
    }

    companion object {
        private const val CONTEXT_CLIENT_VERSION = 3
    }
}