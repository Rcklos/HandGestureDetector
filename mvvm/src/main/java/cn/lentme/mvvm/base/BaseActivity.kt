package cn.lentme.mvvm.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<T: ViewBinding, VM: BaseViewModel>: AppCompatActivity() {

    private lateinit var _binding: T
    protected val mBinding get() = _binding

    private lateinit var _viewModel: VM
    protected val mViewModel get() = _viewModel


    abstract fun fetchBinding(): T
    abstract fun fetchViewModel(): VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = fetchBinding()
        setContentView(_binding.root)

        _viewModel = fetchViewModel()

        initUI()
        initData()
    }

    open fun initUI() {}
    open fun initData() {}

}