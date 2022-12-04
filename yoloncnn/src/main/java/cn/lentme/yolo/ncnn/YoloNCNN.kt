package cn.lentme.yolo.ncnn

import android.content.res.AssetManager
import android.graphics.Bitmap

class YoloNCNN {

    /**
     * A native method that is implemented by the 'ncnn' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String
    external fun load(assetManager: AssetManager): Boolean
    external fun detect(bitmap: Bitmap): ArrayList<YoloObject>

    companion object {
        // Used to load the 'ncnn' library on application startup.
        init {
            System.loadLibrary("yoloncnn")
        }
    }
}