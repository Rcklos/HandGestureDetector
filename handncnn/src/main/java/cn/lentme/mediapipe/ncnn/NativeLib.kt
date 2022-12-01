package cn.lentme.mediapipe.ncnn

class NativeLib {

    /**
     * A native method that is implemented by the 'ncnn' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'ncnn' library on application startup.
        init {
            System.loadLibrary("ncnn")
        }
    }
}