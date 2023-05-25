package com.yuanquan.common.utils

import android.util.Log

object LogUtil {
    private const val TAG = "LogUtil-LOG"
    const val TAG_NET = "LogUtil-NET"

    @JvmStatic
    fun i(message: Any?) {
        Log.i(TAG, message.toString())
    }

    @JvmStatic
    fun i(tag: String, message: Any?) {
        Log.i(tag, message.toString())
    }

    @JvmStatic
    fun d(tag: String, message: Any?) {
        Log.i(tag, message.toString())
    }

    @JvmStatic
    fun w(tag: String, message: Any?) {
        Log.w(tag, message.toString())
    }

    @JvmStatic
    fun e(message: Any?) {
        Log.e(TAG, message.toString())
    }
}