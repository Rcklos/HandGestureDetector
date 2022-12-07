package cn.lentme.hand.detector.request.viewmodel
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.Size
import androidx.lifecycle.MutableLiveData
import cn.lentme.hand.detector.entity.HandDetectResult
import cn.lentme.hand.detector.detect.AbstractHandDetectManager
import cn.lentme.hand.detector.detect.AbstractYoloDetectManager
import cn.lentme.hand.detector.request.repository.HandSelectorRepository
import cn.lentme.mvvm.base.BaseViewModel

class MainViewModel(private val repository: HandSelectorRepository,
                    private val handDetectManager: AbstractHandDetectManager,
                    private val yoloDetectManager: AbstractYoloDetectManager): BaseViewModel() {

    val gesture by lazy { MutableLiveData("None") }
    val selected by lazy { MutableLiveData<Bitmap>(null)}

    fun detectHandAndDraw(bitmap: Bitmap) = handDetectManager.detectAndDraw(bitmap)
    fun computeHandGesture(angles: List<Double>) = AbstractHandDetectManager.computeHandGesture(angles)
    fun updateHandSelector(bitmap: Bitmap, result: HandDetectResult,
                           gesture: String): RectF? {
        val canvas = Canvas(bitmap)
        val screenSize = Size(bitmap.width, bitmap.height)
        return repository.updateHandSelector(canvas, screenSize, result, gesture)
    }
    fun detectYolo(bitmap: Bitmap, screenSize: Size) = yoloDetectManager.detect(bitmap, screenSize)
}