package cn.lentme.hand.detector.util

import android.widget.Toast
import cn.lentme.hand.detector.app.App

object CommonUtils {
    fun showToast(message: String) {
        Toast.makeText(
            App.instance, message,
            Toast.LENGTH_SHORT
        ).show()
    }
}