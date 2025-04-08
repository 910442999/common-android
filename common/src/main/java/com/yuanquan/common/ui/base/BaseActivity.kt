package com.yuanquan.common.ui.base

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.yuanquan.common.api.error.ErrorResult
import com.yuanquan.common.event.EventCode
import com.yuanquan.common.event.EventMessage
import com.yuanquan.common.utils.*
import com.yuanquan.common.widget.dialog.LoadingDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * # 保持ViewModel和ViewBinding不混淆，否则无法反射自动创建
 * -keep class * implements androidx.viewbinding.ViewBinding { *; }
 * -keep class * extends androidx.lifecycle.ViewModel { *; }
 */
abstract class BaseActivity<VM : BaseViewModel<VB>, VB : ViewBinding> : AppCompatActivity() {
    var TAG = getActivityName()
    lateinit var mContext: FragmentActivity
    lateinit var vm: VM
    lateinit var vb: VB

    private var loadingDialog: LoadingDialog? = null

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initResources()
        var pathfinders = ArrayList<GenericParadigmUtil.Pathfinder>()
        pathfinders.add(GenericParadigmUtil.Pathfinder(0, 0))
        val clazzVM = GenericParadigmUtil.parseGenericParadigm(javaClass, pathfinders) as Class<VM>
        vm = ViewModelProvider(this).get(clazzVM)
        pathfinders = ArrayList()
        pathfinders.add(GenericParadigmUtil.Pathfinder(0, 1))
        val clazzVB = GenericParadigmUtil.parseGenericParadigm(javaClass, pathfinders)
        val method = clazzVB.getMethod("inflate", LayoutInflater::class.java)
        vb = method.invoke(null, layoutInflater) as VB
        vm.binding(vb)
        vm.observe(this, this)
        setContentView(vb.root)

        mContext = this
        init()
        initView()
        loadingDialog = LoadingDialog(this)
        initClick()
        initData()
        if (theScreenIsAlwaysOn()) SysUtils.theScreenIsAlwaysOn(mContext, true)
    }

    /**
     * 屏幕常亮
     */
    protected open fun theScreenIsAlwaysOn(): Boolean {
        return false
    }

    /**
     * 防止系统字体影响到app的字体
     *
     * @return
     */
    open fun initResources(): Resources? {
        val res: Resources = super.getResources()
        val config = Configuration()
        config.setToDefaults()
        res.updateConfiguration(config, res.displayMetrics)
        return res
    }

    override fun onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this)
        dismissLoading()
        loadingDialog == null
        if (theScreenIsAlwaysOn()) SysUtils.theScreenIsAlwaysOn(mContext, false)
        super.onDestroy()
    }

    //事件传递
    @Subscribe
    fun onEventMainThread(event: EventMessage) {
        handleEvent(event)
    }

    open fun getClassName(): String {
        val className = "${this.packageName}.ui.base.BaseActivity"
        try {
            return javaClass.name
        } catch (e: Exception) {
        }
        return className
    }

    open fun getActivityName(): String {
        val className = "BaseActivity"
        try {
            return javaClass.simpleName
        } catch (e: Exception) {
        }
        return className
    }

    open fun onPageName(): String? {
        return getActivityName()
    }

    abstract fun initView()

    open fun initClick() {}

    abstract fun initData()

    private fun init() {
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
        //loading
        (vm as BaseViewModel<*>).isShowLoading.observe(this, Observer {
            if (it) showLoading() else dismissLoading()
        })
        //错误信息
        (vm as BaseViewModel<*>).errorData.observe(this, Observer {
            if (it.show) showToast(it.errMsg)
            errorResult(it)
        })
    }

    open fun showLoading() {
        if (loadingDialog != null && !loadingDialog!!.isShowing) {
            loadingDialog?.setCancelable(false)
            loadingDialog?.setCanceledOnTouchOutside(false)
            loadingDialog?.show()
        }
    }

    open fun dismissLoading() {
        if (loadingDialog != null && loadingDialog!!.isShowing) {
            loadingDialog?.dismiss()
        }
    }

    open fun showDialog(context: Context?, s: String?, onClickListener: View.OnClickListener) {
        if (!TextUtils.isEmpty(s)) {
            AlertDialogUtils.show(context, s) { view ->
                AlertDialogUtils.dismiss()
                onClickListener.onClick(view)
            }
        }
    }

    open fun dismissDialog() {
        AlertDialogUtils.dismiss()
    }

    open fun showToast(s: String?) {
        if (!TextUtils.isEmpty(s)) {
            ToastUtils.show(mContext, s)
        }
    }

    /**
     * 消息、事件接收回调
     */
    open fun handleEvent(event: EventMessage) {
        if (event.code == EventCode.LOGIN_OUT) {
            finish()
        }
    }

    /**
     * 接口请求错误回调
     */
    open fun errorResult(errorResult: ErrorResult) {}
    open fun finish(view: View?) {
        finish()
    }

    //国际化
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }
}