package cn.lentme.hand.detector.detect.state.listener

import android.content.Intent
import android.widget.Toast
import cn.lentme.hand.detector.activity.GLRenderActivity
import cn.lentme.hand.detector.app.App
import cn.lentme.hand.detector.app.TtsManager
import cn.lentme.hand.detector.detect.state.DefaultStateChangeListener
import cn.lentme.hand.detector.detect.state.HandState
import org.koin.java.KoinJavaComponent.inject

class ChangeToRenderStateChangeListener: DefaultStateChangeListener() {

    private val ttsManager: TtsManager by inject(TtsManager::class.java)

    override fun consume(updateData: HandState.UpdateData): Boolean {
        val activity = App.instance.activity!!
        val intent = Intent(activity, GLRenderActivity::class.java)
        activity.runOnUiThread {
            activity.startActivity(intent)
            ttsManager.speakText("切换3D看房模式")
            Toast.makeText(App.instance, "start activity", Toast.LENGTH_SHORT).show()
        }
        return false
    }
}