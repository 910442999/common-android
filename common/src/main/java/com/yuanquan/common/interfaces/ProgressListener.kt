package com.yuanquan.common.interfaces

/**
 * 进度和 MD5 计算监听接口
 */
interface ProgressListener {
    /**
     * 进度更新回调
     * @param progress 当前进度 (0-100)
     */
    fun onProgress(progress: Int)
}