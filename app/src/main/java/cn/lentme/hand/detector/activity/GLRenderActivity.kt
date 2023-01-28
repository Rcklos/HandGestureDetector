package cn.lentme.hand.detector.activity

import android.app.ActivityManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.camera.core.CameraSelector
import cn.lentme.allncnn.Point2f
import cn.lentme.gles.render.GL_SIMPLE_CAMERA2
import cn.lentme.gles.render.GL_SIMPLE_CAMERA4
import cn.lentme.gles.render.MyNativeRender
import cn.lentme.gles.render.ui.MyGLSurfaceView
import cn.lentme.hand.detector.app.App
import cn.lentme.hand.detector.app.TtsManager
import cn.lentme.hand.detector.camera.CameraHelper
import cn.lentme.hand.detector.camera.CameraLifecycle
import cn.lentme.hand.detector.databinding.ActivityRenderBinding
import cn.lentme.hand.detector.detect.AbstractHandDetectManager
import cn.lentme.hand.detector.detect.HandDetectManager
import cn.lentme.hand.detector.detect.state.DefaultStateChangeListener
import cn.lentme.hand.detector.detect.state.HandState
import cn.lentme.hand.detector.request.viewmodel.GLRenderViewModel
import cn.lentme.mvvm.base.BaseActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class GLRenderActivity: BaseActivity<ActivityRenderBinding, GLRenderViewModel>() {

    override fun fetchBinding(): ActivityRenderBinding =
        ActivityRenderBinding.inflate(layoutInflater)

    override fun fetchViewModel(): GLRenderViewModel = getViewModel()

    private lateinit var mRender: MyNativeRender
    private lateinit var mSurface: MyGLSurfaceView
    private lateinit var mCursorSurface: SurfaceView

    private val queue = ConcurrentLinkedQueue<Point2f>()
    private val handDetector: HandDetectManager by inject()
    private val ttsManager: TtsManager by inject()

    private lateinit var cameraHelper: CameraHelper
    private val lifecycle = CameraLifecycle()

    private val handState = HandState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set view
        setContentView(mBinding.root)

        // init cursor
        initCursor()

        // 摄像机
        cameraHelper = CameraHelper(
            lifecycle, CameraSelector.DEFAULT_FRONT_CAMERA, buildProcessor
        )

        // 初始化OpenGL ES
        initSurface(GL_SIMPLE_CAMERA4)

        // 注册退出事件
        handState.registerEvent(AbstractHandDetectManager.GESTURE_SIX,
            object: DefaultStateChangeListener() {
                override fun consume(updateData: HandState.UpdateData): Boolean {
                    App.instance.activity!!.runOnUiThread {
                        ttsManager.speakText("正在退出看房模式")
                        finish()
                    }
                    return false
                }
        })
    }

    private fun initCursor() {
        mCursorSurface = mBinding.surfaceCursor
        mCursorSurface.holder.addCallback(cursorSurfaceCallback)
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

    private val cursorSurfaceCallback = object: SurfaceHolder.Callback {
        private var thread: CursorDrawThread? = null
        private val atomic = AtomicInteger(0)
        override fun surfaceCreated(holder: SurfaceHolder) {
            Log.d(TAG, "[thread: ${Thread.currentThread().id}] cursor surface created")
            thread = CursorDrawThread(holder, 0, 0)
            val set = atomic.incrementAndGet()
            if(set == 1) {
                thread!!.start()
                Log.d(TAG, "cursor draw thread started!!!")
            }
            else atomic.set(0)
            Log.d(TAG, "created set: $set")
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            Log.d(TAG, "[thread: ${Thread.currentThread().id}] cursor surface changed")
            thread?.let {
                it.width = width
                it.height = height
                Log.d(TAG, "cursor draw thread resize with [w: $width, h: $height]")
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            Log.d(TAG, "[thread: ${Thread.currentThread().id}] cursor surface destroy")
            val set = atomic.incrementAndGet()
            if(set == 2) {
                thread!!.interrupt()
                Log.d(TAG, "cursor draw thread is interrupted!!!")
            }
            Log.d(TAG, "destroyed set: $set")
        }
    }

    private inner class CursorDrawThread(
        private val holder: SurfaceHolder,
        var width: Int, var height: Int
        ) : Thread() {

        var isVisible = true
        private val paint = Paint()
        private var cursor = Point2f(0.5f, 0.5f)
        private var lastPoint2f = Point2f(-1.0f, -1.0f)

        init {
            paint.color = Color.BLUE
            paint.isAntiAlias = true
            paint.strokeWidth = 12.0f

            holder.setFormat(PixelFormat.TRANSPARENT)
        }

        override fun run() {
            while (true) {
                if(currentThread().isInterrupted) break
//                Log.d(TAG, "cursor draw thread is waiting for interruption")
                if(width == 0 && height == 0) continue
                val canvas = holder.lockCanvas() ?: continue
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
//                Log.d(TAG, "queue size: ${queue.size}")
                if (queue.isNotEmpty()){
                    val point = queue.poll()!!
                    val x = point.x
                    val y = point.y
                    cursor = if((x != -1.0f && y != -1.0f) && lastPoint2f.x != -1.0f) {
                        val dx = (x - lastPoint2f.x)
                        val dy = (y - lastPoint2f.y)

                        runOnUiThread {
                            mSurface.setDelta(x, y)
                        }

                        var nx = dx + cursor.x
                        if(nx < 0) nx = 0.0f
                        else if(nx > 1.0f) nx = 1.0f
                        var ny = dy + cursor.y
                        if(ny < 0) ny = 0.0f
                        else if(ny > 1.0f) ny = 1.0f
//                        Log.d(TAG, "cursor: x = ${cursor.x}, y = ${cursor.y}, dx = $dx, dy = $dy")
                        Point2f(nx, ny)
//                        Point2f(x, y)
                    } else {
                        runOnUiThread {
                            mSurface.setDelta(-1.0f, -1.0f)
                        }
                        cursor
                    }
                    lastPoint2f = Point2f(x, y)
                }
                if (isVisible)
                    canvas.drawPoint(cursor.x * width, cursor.y * height, paint)
                holder.unlockCanvasAndPost(canvas)
            }
            Log.d(TAG, "cursor draw thread: bye!")
        }
    }

    private val buildProcessor = object: CameraHelper.Processor{
        override fun process(bitmap: Bitmap) {
            // 检测手
            val result = handDetector.detect(bitmap)
            if(result.points.isNotEmpty()) {
                val gesture = AbstractHandDetectManager.computeHandGesture(result.angles)
                if(gesture == AbstractHandDetectManager.GESTURE_ONE)
                    queue.offer(Point2f(result.points[8].x, result.points[8].y))
                else
                    queue.offer(Point2f(-1.0f, -1.0f))

                when (gesture) {
                    AbstractHandDetectManager.GESTURE_FIVE -> {
                        runOnUiThread {
                            mSurface.setDirection(MyGLSurfaceView.DIRECTION_UP)
                        }
                    }
                    AbstractHandDetectManager.GESTURE_ZERO -> {
                        runOnUiThread {
                            mSurface.setDirection(MyGLSurfaceView.DIRECTION_DOWN)
                        }
                    }
                    else -> {
                        runOnUiThread {
                            mSurface.setDirection(MyGLSurfaceView.DIRECTION_NONE)
                        }
                    }
                }
            } else {
                queue.offer(Point2f(-1.0f, -1.0f))
                runOnUiThread {
                    mSurface.setDirection(MyGLSurfaceView.DIRECTION_NONE)
                }
            }

            // 退出状态监听
            handState.process(
                HandState.UpdateData(
                result.bitmap, result
            ))

            runOnUiThread {
                mBinding.imageTest.setImageBitmap(result.bitmap)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // surface
        mSurface.onResume()
        mRender.copyAssetsToCache(this)

        // 摄像头
        lifecycle.cameraOnCreate()
        lifecycle.cameraOnStart()
        lifecycle.cameraOnResume()
        cameraHelper.bindCamera()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        lifecycle.cameraOnPause()
        lifecycle.cameraOnStop()
        lifecycle.cameraOnDestroyed()
        Log.d(TAG, "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        queue.clear()
        cameraHelper.shutDown()
    }

    private fun detectOpenGLES30(): Boolean {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val info = am.deviceConfigurationInfo
        return info.reqGlEsVersion >= 0x30000
    }

    companion object {
        private const val CONTEXT_CLIENT_VERSION = 3
        private const val TAG = "GLRenderActivity"
    }
}