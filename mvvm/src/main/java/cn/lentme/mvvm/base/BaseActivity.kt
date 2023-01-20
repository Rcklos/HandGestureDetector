package cn.lentme.mvvm.base

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<T: ViewBinding, VM: BaseViewModel>: AppCompatActivity() {

    private lateinit var _binding: T
    protected val mBinding get() = _binding

    private lateinit var _viewModel: VM
    protected val mViewModel get() = _viewModel

    private var callBackOnRequestPermisionsResult = fun() {
        Toast.makeText(this,
            "default callback on request permissions.",
            Toast.LENGTH_SHORT
        ).show()
    }

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

    override fun onResume() {
        super.onResume()
        // 设置activity
        (application as BaseApplication).activity = this
    }

    protected fun requestPermissions(
        permissions: Array<String>,
        callBack: () -> Unit
    ) {
        callBackOnRequestPermisionsResult = callBack
        ActivityCompat.requestPermissions(
            this, permissions, REQUEST_CODE_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_PERMISSIONS)
            callBackOnRequestPermisionsResult()
    }

    protected fun isPermissionGranted(permissions: Array<String>): Boolean =
        permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    open fun initUI() {}
    open fun initData() {}

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

}