package cn.lentme.hand.detector.request.viewmodel
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.Size
import androidx.lifecycle.MutableLiveData
import cn.lentme.hand.detector.app.App
import cn.lentme.hand.detector.hand.legecy.HandDetectManager
import cn.lentme.hand.detector.entity.HandDetectResult
import cn.lentme.hand.detector.hand.AbstractHandDetectManager
import cn.lentme.hand.detector.request.repository.HandSelectorRepository
import cn.lentme.mvvm.base.BaseViewModel

class MainViewModel(private val repository: HandSelectorRepository,
                    private val handDetectManager: AbstractHandDetectManager): BaseViewModel() {
    val hello by lazy { MutableLiveData("hello world") }
    val gesture by lazy { MutableLiveData("None") }
    val selected by lazy { MutableLiveData<Bitmap>(null)}

//    private val handDetectManager = HandDetectManager(App.instance.applicationContext)

    fun detectHandAndDraw(bitmap: Bitmap) = handDetectManager.detectAndDraw(bitmap)
    fun computeHandGesture(angles: List<Double>) = AbstractHandDetectManager.computeHandGesture(angles)
    fun updateHandSelector(bitmap: Bitmap, result: HandDetectResult,
                           gesture: String): RectF? {
        val canvas = Canvas(bitmap)
        val screenSize = Size(bitmap.width, bitmap.height)
        return repository.updateHandSelector(canvas, screenSize, result, gesture)
    }
}