package com.yuanquan.common.api

import android.text.TextUtils
import com.yuanquan.common.utils.SPUtils

object URLConstant {
    @JvmStatic
    fun getHost(): String {
        return "https://www.wanandroid.com/";
    }

    @JvmStatic
    fun isTest(): Boolean {
        return !TextUtils.isEmpty(SPUtils.getInstance().getString("host"))
    }
}