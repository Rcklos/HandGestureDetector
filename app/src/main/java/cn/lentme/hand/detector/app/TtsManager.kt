package cn.lentme.hand.detector.app

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

class TtsManager(context: Context) {
    private var mSpeech: TextToSpeech? = null
    var mSpeechListener: UtteranceProgressListener? = null
    private var mInited = false

    fun speakText(text: String): Boolean {
        if(!mInited) return false
        var result = TextToSpeech.ERROR
        mSpeech?.run {
            result = speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
        return result == TextToSpeech.SUCCESS
    }

    init {
        destory()
        mSpeech = TextToSpeech(context) { status ->
            if(status != TextToSpeech.SUCCESS) {
                Log.e(TAG, "TtsManager init failed!!!")
                return@TextToSpeech
            }
            mSpeech?.apply {
                val result = setLanguage(Locale.getDefault())
                if(result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "TtsManager language set failed!!!!")
                    return@TextToSpeech
                }
                setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        mSpeechListener?.apply { onStart(utteranceId) }
                    }

                    override fun onDone(utteranceId: String?) {
                        mSpeechListener?.apply { onDone(utteranceId) }
                    }

                    override fun onError(utteranceId: String?) {
                        mSpeechListener?.apply { onError(utteranceId) }
                    }
                })
                // 初始化成功
                Log.d(TAG, "init successfully")
                mInited = true
            }
        }
    }

    fun destory() {
        mSpeech?.apply{
            stop()
            shutdown()
        }
    }

    fun stop() {
        mSpeech?.apply {
            if(isSpeaking) stop()
        }
    }

    fun isSpeaking(): Boolean = (mSpeech != null && mSpeech!!.isSpeaking)

    companion object {
        private const val TAG = "TtsManager"
    }
}