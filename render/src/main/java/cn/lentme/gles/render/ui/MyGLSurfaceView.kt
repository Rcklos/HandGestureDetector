package cn.lentme.gles.render.ui

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import cn.lentme.gles.render.GL_BASE_TYPE
import cn.lentme.gles.render.MyNativeRender

class MyGLSurfaceView: GLSurfaceView {
    constructor(context: Context?): super(context) {}
    constructor(context: Context?, attrs: AttributeSet?): super(context, attrs) {}

    private var render: MyNativeRender? = null

    companion object {
        private const val BASE_DIRECTION    = 200
        const val DIRECTION_NONE            = (BASE_DIRECTION + 1)
        const val DIRECTION_UP              = (BASE_DIRECTION + 2)
        const val DIRECTION_DOWN            = (BASE_DIRECTION + 3)
        const val DIRECTION_LEFT            = (BASE_DIRECTION + 4)
        const val DIRECTION_RIGHT           = (BASE_DIRECTION + 5)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            val dx: Float = event.x / width;
            val dy: Float = event.y / height;
            when(it.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    setDelta(dx, dy)
                    return true
                }
                MotionEvent.ACTION_UP ->
                    setDelta(-1.0f, -1.0f)
            }
        }
        return false
    }

    override fun setRenderer(renderer: Renderer?) {
        super.setRenderer(renderer)
        if(renderer is MyNativeRender) {
            render = renderer
        }
    }

    fun setDelta(x: Float, y: Float) {
        render?.run{
            setDelta(x, y)
        }
    }

    fun setDirection(dir: Int) {
        when(dir) {
            DIRECTION_NONE,
            DIRECTION_LEFT,
            DIRECTION_RIGHT,
            DIRECTION_UP,
            DIRECTION_DOWN -> render?.setDirection(dir)
            else -> render?.setDirection(DIRECTION_NONE)
        }
    }
}