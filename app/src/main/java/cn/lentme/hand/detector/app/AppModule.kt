package cn.lentme.hand.detector.app

import cn.lentme.hand.detector.hand.AbstractHandDetectManager
import cn.lentme.hand.detector.hand.legecy.HandDetectManager
import cn.lentme.hand.detector.request.repository.HandSelectorRepository
import cn.lentme.hand.detector.request.viewmodel.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

/**
 * Created by rcklos on 2022/5/15 03点02分
 */
val repositoryModel = module {
    single { HandSelectorRepository() }
    single { HandDetectManager(App.instance.applicationContext) } withOptions {
        bind<AbstractHandDetectManager>()
        createdAtStart()
    }
}

val viewModelModule = module {
    viewModel { MainViewModel(get(), get()) }
}

val appModule = listOf(viewModelModule, repositoryModel)