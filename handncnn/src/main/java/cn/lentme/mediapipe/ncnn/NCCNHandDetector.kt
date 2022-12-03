package cn.lentme.mediapipe.ncnn

import android.content.res.AssetManager
import android.graphics.Bitmap

class NCCNHandDetector {

    /**
     * A native method that is implemented by the 'ncnn' native library,
     * which is packaged with this application.
     */
    external fun load(assetManager: AssetManager): Boolean
    external fun detect(bitmap: Bitmap): DetectResult

    companion object {
        // Used to load the 'handncnn' library on application startup.
        init {
            System.loadLibrary("handncnn")
        }
    }
}