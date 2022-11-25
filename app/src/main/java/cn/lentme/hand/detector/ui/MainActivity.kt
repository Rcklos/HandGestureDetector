package cn.lentme.hand.detector.ui

import cn.lentme.hand.detector.databinding.ActivityMainBinding
import cn.lentme.hand.detector.request.viewmodel.MainViewModel
import cn.lentme.mvvm.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity: BaseActivity<ActivityMainBinding, MainViewModel>() {
    override fun fetchBinding() = ActivityMainBinding.inflate(layoutInflater)
    override fun fetchViewModel(): MainViewModel = getViewModel()

    override fun initData() {
        mViewModel.hello.observe(this) { mBinding.hello.text = it }
    }
}