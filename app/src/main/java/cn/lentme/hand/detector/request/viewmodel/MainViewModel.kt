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

class MainViewModel(private val handDetectManager: AbstractHandDetectManager): BaseViewModel() {

    val cropBitmap by lazy { MutableLiveData<Bitmap>(null) }

    fun detectHand(bitmap: Bitmap) = handDetectManager.detect(bitmap)
}