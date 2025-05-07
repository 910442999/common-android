package com.yuanquan.common.interfaces

import java.io.File

/**
 * 进度监听接口
 */
interface OnDownloadListener {
    fun onProgress(progress: Int, bytesWritten: Long, totalBytes: Long)
    fun onComplete(file: File)
    fun onError(t: Throwable)
    fun onStart()
    fun onCancel()
}