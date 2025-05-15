package com.yuanquan.common.ui.base

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.viewbinding.ViewBinding
import com.yuanquan.common.api.error.ErrorResult
import com.yuanquan.common.api.response.BaseResult
import com.yuanquan.common.utils.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.collections.set


open class BaseViewModel<VB : ViewBinding> : ViewModel() {

    private val AUTH_SECRET = "123456"//前后台协议密钥

    var isShowLoading = MutableLiveData<ErrorResult>()//是否显示loading
    var errorData = MutableLiveData<ErrorResult>()//错误信息
    private var errorResult = ErrorResult()
    lateinit var vb: VB

    fun binding(vb: VB) {
        this.vb = vb
    }

    open fun observe(activity: Activity, owner: LifecycleOwner) {

    }

    open fun observe(fragment: Fragment, owner: LifecycleOwner) {

    }

    private fun showLoading(message: String? = null) {
        errorResult.showLoading = true
        errorResult.errMsg = message
        isShowLoading.value = errorResult
    }

    private fun dismissLoading() {
        errorResult.showLoading = false
        errorResult.errMsg = null
        isShowLoading.value = errorResult
    }

    private fun showError(error: ErrorResult) {
        errorData.value = error
    }

    /**
     * 无参
     */
    open fun signNoParams(): LinkedHashMap<String, String?> {
        var params = LinkedHashMap<String, String?>()
        params["sign"] = getSign(params)
        return params
    }

    /**
     * 有参
     */
    open fun signParams(params: LinkedHashMap<String, String?>): LinkedHashMap<String, String?> {
        params["sign"] = getSign(params)
        return params
    }


    /**
     * 签名
     */
    private fun getSign(params: LinkedHashMap<String, String?>): String {
        val sb = StringBuilder()
        params.forEach {
            val key = it.key
            var value = ""
            if (!it.value.isNullOrEmpty()) {
                value = URLEncoder.encode(it.value as String?).replace("\\+", "%20")
            }
            sb.append("$key=$value&")
        }
        val s = sb.toString().substring(0, sb.toString().length - 1).toLowerCase() + AUTH_SECRET
        return encryption(s)
    }


    /**
     * MD5加密
     *
     * @param plainText 明文
     * @return 32位密文
     */
    private fun encryption(plainText: String): String {
        var re_md5 = ""
        try {
            val md: MessageDigest = MessageDigest.getInstance("MD5")
            md.update(plainText.toByteArray())
            val b: ByteArray = md.digest()
            var i: Int
            val buf = StringBuffer("")
            for (offset in b.indices) {
                i = b[offset].toInt()
                if (i < 0) i += 256
                if (i < 16) buf.append("0")
                buf.append(Integer.toHexString(i))
            }
            re_md5 = buf.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return re_md5
    }

    /**
     * 请求接口，可定制是否显示loading和错误提示
     */
    open fun <T> launch(
        block: suspend CoroutineScope.() -> BaseResult<T>,//请求接口方法，T表示data实体泛型，调用时可将data对应的bean传入即可
        liveData: MutableLiveData<T>,
        isShowLoading: Boolean = true,
        isShowError: Boolean = true,
        code: Int = 0,
        index: Int = 0,
        method: String? = null,
    ) {
        if (isShowLoading) showLoading()
        viewModelScope.launch {
            try {
                val result = block()
                if (result.code == code) {//请求成功
                    liveData.value = result.data
                } else {
                    var message: String? =
                        if (result.message.isNullOrBlank()) result.msg else result.message
                    showError(
                        ErrorResult(
                            result.code, message, isShowError, index = index,
                            method = method
                        )
                    )
                }
            } catch (e: Exception) {//接口请求失败
                val errorResult = ErrorResult()
                if (e is HttpException) {
                    errorResult.code = e.code()
                    LogUtil.e("请求异常>>$e")
                } else {
                    errorResult.code = 500
                }
                LogUtil.e("请求异常>>$e")
                errorResult.errMsg = e.message
//                if (errorResult.errMsg.isNullOrEmpty()) errorResult.errMsg = "网络请求失败，请重试"
                errorResult.showToast = isShowError
                errorResult.index = index
                errorResult.method = method
                showError(errorResult)
            } finally {//请求结束
                if (isShowLoading) dismissLoading()
            }
        }
    }
}
