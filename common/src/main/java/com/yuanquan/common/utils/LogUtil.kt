package com.yuanquan.common.utils

import android.util.Log
import com.yuanquan.common.BuildConfig

object LogUtil {
    private const val TAG = "OOOK-LOG"
    const val TAG_NET = "OOOK-NET"

    @JvmStatic
    fun i(message: Any?) {
        if (BuildConfig.DEBUG) Log.i(TAG, message.toString())
    }

    @JvmStatic
    fun i(tag: String, message: Any?) {
        if (BuildConfig.DEBUG) Log.i(tag, message.toString())
    }

    @JvmStatic
    fun d(tag: String, message: Any?) {
        if (BuildConfig.DEBUG) Log.i(tag, message.toString())
    }

    @JvmStatic
    fun w(tag: String, message: Any?) {
        if (BuildConfig.DEBUG) Log.w(tag, message.toString())
    }

    @JvmStatic
    fun e(message: Any?) {
        if (BuildConfig.DEBUG) Log.e(TAG, message.toString())
    }
}