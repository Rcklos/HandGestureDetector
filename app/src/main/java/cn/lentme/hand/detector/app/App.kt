package cn.lentme.hand.detector.app

import cn.lentme.mvvm.base.BaseApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.stopKoin

class App: BaseApplication() {

    companion object {
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        org.koin.core.context.startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule)
        }
    }
}
