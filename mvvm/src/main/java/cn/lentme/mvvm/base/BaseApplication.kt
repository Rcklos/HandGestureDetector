package cn.lentme.mvvm.base

import android.app.Activity
import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.Module

abstract class BaseApplication(
    var activity: Activity? = null
): Application()