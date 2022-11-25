package cn.lentme.mvvm.base

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

open class BaseViewModel: ViewModel(), LifecycleObserver {
    private val _tag = "BaseViewModel"

    fun launchUI(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch {
        try {
            withTimeout(15 * 1000) {
                block()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}