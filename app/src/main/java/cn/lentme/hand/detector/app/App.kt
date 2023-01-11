package cn.lentme.hand.detector.app

import android.app.Activity
import android.app.Application
import cn.lentme.mvvm.base.BaseApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.Module

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
