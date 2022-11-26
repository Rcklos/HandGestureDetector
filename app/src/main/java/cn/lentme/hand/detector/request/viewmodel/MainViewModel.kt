package cn.lentme.hand.detector.request.viewmodel
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import cn.lentme.hand.detector.app.App
import cn.lentme.hand.detector.app.HandDetectManager
import cn.lentme.mvvm.base.BaseViewModel

class MainViewModel: BaseViewModel() {
    val hello by lazy { MutableLiveData("hello world") }
    val gesture by lazy { MutableLiveData("None") }

    private val handDetectManager = HandDetectManager(App.instance.applicationContext)

    fun detectHandAndDraw(bitmap: Bitmap) = handDetectManager.detectAndDraw(bitmap)
    fun computeHandGesture(angles: List<Double>) = handDetectManager.computeHandGesture(angles)
}