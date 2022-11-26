package cn.lentme.hand.detector.request.viewmodel
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import cn.lentme.mvvm.base.BaseViewModel

class MainViewModel: BaseViewModel() {
    val hello by lazy { MutableLiveData("hello world") }
}