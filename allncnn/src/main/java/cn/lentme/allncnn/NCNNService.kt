package cn.lentme.allncnn

import android.content.res.AssetManager
import android.graphics.Bitmap

class NCNNService {

    /**
     * A native method that is implemented by the 'allncnn' native library,
     * which is packaged with this application.
     */
    external fun loadHandDetector(assetManager: AssetManager): Boolean
    external fun detectHand(bitmap: Bitmap): DetectResult

    external fun loadYoloDetector(assetManager: AssetManager): Boolean
    external fun detectYolo(bitmap: Bitmap): ArrayList<YoloObject>

    external fun displayPointer(display: Boolean)

    companion object {
        // Used to load the 'allncnn' library on application startup.
        init {
            System.loadLibrary("allncnn")
        }
    }
}