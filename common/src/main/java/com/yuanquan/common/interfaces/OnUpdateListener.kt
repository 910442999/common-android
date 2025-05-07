package com.yuanquan.common.interfaces

interface OnUpdateListener {
    fun onStart()
    fun onProgress(progress: Int)
    fun onComplete(url: Any)
    fun onDisposed(message: String?)
    fun onError(message: String?)
}