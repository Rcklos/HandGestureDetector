package cn.lentme.gles.render.util

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLUtils
import java.io.IOError
import java.io.IOException
import java.io.InputStream

class Utils {
    companion object {
        @JvmStatic
        fun loadTexture(assetManager: AssetManager, path: String): Int {
            var input: InputStream
            try {
                input = assetManager.open(path)
            } catch (ex: Exception) {
                ex.printStackTrace()
                return -1
            }
            val op = BitmapFactory.Options()
            op.inPreferredConfig = Bitmap.Config.ARGB_8888
            val bmp = BitmapFactory.decodeStream(input, null, op)

            val texture = IntArray(1) { 0 }
            GLES30.glGenTextures(1, texture, 0)
            val textureID = texture[0]
            GLES30.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST)
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bmp, 0)

            try {
                input.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                bmp?.recycle()
            }

            return textureID;
        }
    }
}