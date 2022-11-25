package cn.lentme.mvvm.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<T: ViewBinding, VM: BaseViewModel>: Fragment() {

    private lateinit var _binding: T
    protected val mBinding get() = _binding

    private lateinit var _viewModel: VM
    protected val mViewModel get() = _viewModel


    abstract fun fetchBinding(): T
    abstract fun fetchViewModel(): VM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = fetchBinding()
        _viewModel = fetchViewModel()

        initUI()
        initData()
        return _binding.root
    }

    open fun initUI() {}
    open fun initData() {}

}