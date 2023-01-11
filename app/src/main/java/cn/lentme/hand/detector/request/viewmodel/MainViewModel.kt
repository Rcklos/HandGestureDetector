package cn.lentme.hand.detector.request.viewmodel
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.Size
import androidx.lifecycle.MutableLiveData
import cn.lentme.hand.detector.app.TtsManager
import cn.lentme.hand.detector.entity.HandDetectResult
import cn.lentme.hand.detector.detect.AbstractHandDetectManager
import cn.lentme.hand.detector.detect.AbstractYoloDetectManager
import cn.lentme.hand.detector.request.repository.HandSelectorRepository
import cn.lentme.mvvm.base.BaseViewModel

class MainViewModel(private val repository: HandSelectorRepository,
                    private val handDetectManager: AbstractHandDetectManager,
                    private val yoloDetectManager: AbstractYoloDetectManager,
                    private val ttsManager: TtsManager): BaseViewModel() {

    val gesture by lazy { MutableLiveData("None") }
    val cropBitmap by lazy { MutableLiveData<Bitmap>(null) }
    val selected by lazy { MutableLiveData<Boolean>(false) }

    fun setSelected(select: Boolean) {
        launchUI {
            selected.value = select
        }
    }

    fun detectHand(bitmap: Bitmap) = handDetectManager.detectHand(bitmap)
    fun computeHandGesture(angles: List<Double>) = AbstractHandDetectManager.computeHandGesture(angles)
    fun updateHandSelector(bitmap: Bitmap, result: HandDetectResult,
                           gesture: String): RectF? {
        val canvas = Canvas(bitmap)
        val screenSize = Size(bitmap.width, bitmap.height)
        return repository.updateHandSelector(canvas, screenSize, result, gesture)
    }
    fun detectYolo(bitmap: Bitmap) = yoloDetectManager.detect(bitmap)

    fun speech(text: String): Boolean = ttsManager.speakText(text)
}