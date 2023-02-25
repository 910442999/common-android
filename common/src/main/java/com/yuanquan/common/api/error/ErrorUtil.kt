package com.yuanquan.common.api.error

import com.yuanquan.common.utils.LogUtil
import retrofit2.HttpException

object ErrorUtil {

    fun getError(e: Exception): ErrorResult {
        val errorResult = ErrorResult()
        if (e is HttpException) {
            errorResult.code = e.code()
        }
        LogUtil.e("请求异常>>$e")
        errorResult.errMsg = e.message
        if (errorResult.errMsg.isNullOrEmpty()) errorResult.errMsg = "网络请求失败，请重试"
        return errorResult
    }

    fun getError(apiIndex: Int, e: Exception): ErrorResult {
        val errorResult = ErrorResult()
        errorResult.index = apiIndex
        if (e is HttpException) {
            errorResult.code = e.code()
        }
        LogUtil.e("请求异常>>$e")
        errorResult.errMsg = e.message
        if (errorResult.errMsg.isNullOrEmpty()) errorResult.errMsg = "网络请求失败，请重试"
        return errorResult
    }

    fun getError(message: String): ErrorResult {
        val errorResult = ErrorResult()
        errorResult.errMsg = message
        if (errorResult.errMsg.isNullOrEmpty()) errorResult.errMsg = "网络请求失败，请重试"
        return errorResult
    }
}