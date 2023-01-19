package cn.lentme.gles.render

import android.content.Context
import android.content.res.AssetManager
import android.opengl.GLSurfaceView
import cn.lentme.gles.render.util.CommonUtils
import java.io.File
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyNativeRender(context: Context) : GLSurfaceView.Renderer{
    // asset，主要用于加载shader、texture、font等资源
    private val mAssetMgr: AssetManager

    /**
     * A native method that is implemented by the 'render' native library,
     * which is packaged with this application.
     */
    // 在Surface创建时调用
    private external fun glesCreate(assetMgr: AssetManager)
    // 在Surface变化时调用
    private external fun glesResize(width: Int, height: Int)
    // 在Surface渲染时调用
    private external fun glesDraw()
    external fun glesSetType(type: Int)
    external fun setDelta(x: Float, y: Float)
    external fun setDirection(dir: Int)

    init {
        mAssetMgr = context.assets
        glesSetType(GL_HELLO_TRIANGLE)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glesCreate(mAssetMgr)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glesResize(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        glesDraw()
    }

    fun copyAssetsToCache(context: Context) {
        val fileDir = context.cacheDir!!.absolutePath
        File(fileDir).let {
            if(!it.exists()) it.mkdirs()
        }

        // 复制assets目录文件到sdcard
        CommonUtils.copyAssetsDirToSDCard(context, "object", fileDir)
    }

    companion object {
        // Used to load the 'render' library on application startup.
        init {
            System.loadLibrary("glnative-render")
        }

        fun loadType(): Array<Pair<String, Int>> = arrayOf(
            // hello系列
            Pair("你好, 三角形", GL_HELLO_TRIANGLE),
            Pair("自定义颜色的三角形", GL_HELLO_TRIANGLE2),
            Pair("VBO和EBO的应用", GL_HELLO_TRIANGLE3),
            Pair("你好，四边形", GL_HELLO_RECTANGLE),
            Pair("两种颜色的三角形绘制一个四边形", GL_HELLO_RECTANGLE2),
            Pair("四边形纹理", GL_HELLO_RECTANGLE3),

            // animation系列
            Pair("简单的缩放动画", GL_SIMPLE_SCALE),
            Pair("简单的位移动画", GL_SIMPLE_TRANSLATE),
            Pair("简单的旋转动画", GL_SIMPLE_ROTATE),

            // 3D
            Pair("你好, 立方体", GL_HELLO_CUBE),
            Pair("立方体简单的MVP变换", GL_HELLO_CUBE2),
            Pair("立方体贴图纹理", GL_HELLO_CUBE3),

            // 模型
            Pair("简单加载模型", GL_SIMPLE_LOAD_MODEL),
            Pair("加载具有材质的模型", GL_SIMPLE_LOAD_MODEL2),

            // 摄像机
            Pair("摄像机围绕模型旋转", GL_SIMPLE_CAMERA),
            Pair("摄像机原地旋转", GL_SIMPLE_CAMERA2),
            Pair("摇杆控制摄像机原地旋转", GL_SIMPLE_CAMERA3),
            Pair("摄像机漫游", GL_SIMPLE_CAMERA4),
        )
    }
}