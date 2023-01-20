package cn.lentme.hand.detector.detect.state.listener

import android.content.Intent
import android.widget.Toast
import cn.lentme.hand.detector.activity.GLRenderActivity
import cn.lentme.hand.detector.activity.MainActivity
import cn.lentme.hand.detector.app.App
import cn.lentme.hand.detector.detect.state.DefaultStateChangeListener
import cn.lentme.hand.detector.detect.state.HandState

class ChangeToRenderStateChangeListener: DefaultStateChangeListener() {
    override fun consume(updateData: HandState.UpdateData): Boolean {
        val activity = App.instance.activity!!
        val intent = Intent(activity, GLRenderActivity::class.java)
        activity.runOnUiThread {
            activity.startActivity(intent)
            Toast.makeText(App.instance, "start activity", Toast.LENGTH_SHORT).show()
        }
        return false
    }
}