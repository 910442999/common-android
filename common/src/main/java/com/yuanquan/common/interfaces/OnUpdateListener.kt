package com.yuanquan.common.interfaces

interface OnUpdateListener {
    fun onStart()
    fun onProgress(progress: Int)
    fun onComplete(data: Any?)
    fun onDisposed(message: String?)
    fun onError(message: String?)
}