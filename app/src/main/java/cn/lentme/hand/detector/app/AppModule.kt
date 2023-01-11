package cn.lentme.hand.detector.app

import cn.lentme.allncnn.NCNNService
import cn.lentme.hand.detector.detect.AbstractHandDetectManager
import cn.lentme.hand.detector.detect.AbstractYoloDetectManager
import cn.lentme.hand.detector.detect.HandDetectManager
import cn.lentme.hand.detector.detect.YoloDetectManager
import cn.lentme.hand.detector.request.repository.HandSelectorRepository
import cn.lentme.hand.detector.detect.state.HandState
import cn.lentme.hand.detector.request.viewmodel.*
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

/**
 * Created by rcklos on 2022/5/15 03点02分
 */
val appModel = module {
    single { TtsManager(App.instance) }
    single { HandState() }
}

val repositoryModel = module {
    single { NCNNService().apply {
        loadHandDetector(App.instance.assets)
        loadYoloDetector(App.instance.assets)
    } }

    single { HandSelectorRepository() }
    single { HandDetectManager(get()) } withOptions {
        bind<AbstractHandDetectManager>()
        createdAtStart()
    }
    single { YoloDetectManager(get()) } withOptions {
        bind<AbstractYoloDetectManager>()
        createdAtStart()
    }
}

val viewModelModule = module {
    viewModel { MainViewModel(get(), get(), get(), get()) }
}

val appModule = listOf(appModel, viewModelModule, repositoryModel)