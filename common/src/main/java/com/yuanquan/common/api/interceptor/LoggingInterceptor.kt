package com.yuanquan.common.api.interceptor

import com.yuanquan.common.api.URLConstant
import com.yuanquan.common.utils.LogUtil
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import java.io.IOException

/**
 * 日志拦截器（支持自定义日志输出）
 *
 * 使用说明：
 * 1. 创建子类继承 LoggingInterceptor
 * 2. 重写 logFinal 方法实现自定义日志输出
 * 3. 在拦截器链中使用自定义子类
 */
open class LoggingInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val httpUrl = request.url
        val startTime = System.currentTimeMillis()
        val path = httpUrl.encodedPath

        // 创建日志构建器
        val builder = StringBuilder().apply {
//            append("\n")
//            append("请求Headers>>> ${request.headers}\n")
            append("请求URL ${request.method} >>> $httpUrl\n")
//            append("API>>> $path\n")
            if (request.method == "POST" || request.method == "PUT") {
                append("请求参数>>> ${bodyToString(request.body)}\n")
            }
        }

        val response = try {
            chain.proceed(request).also { res ->
                // 使用响应副本记录日志，不影响原始响应
                logResponse(res, builder, startTime, path)
            }
        } catch (e: Exception) {
            logError(e, builder, startTime, path)
            throw e
        }

        return response
    }

    /**
     * 记录响应日志
     */
    protected open fun logResponse(
        response: Response,
        builder: StringBuilder,
        startTime: Long,
        path: String
    ) {
        // 创建响应副本，不影响原始响应流
        val responseBody = response.peekBody(Long.MAX_VALUE)
        val result = responseBody.string()
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        val seconds = duration / 1000
        val millis = duration % 1000

        builder.apply {
            append("响应状态码>>> ${response.code}   耗时>>> ${seconds}秒${millis}毫秒\n")
            append("响应结果>>> $result")
        }

        logFinal("INFO", builder, path)
    }

    /**
     * 记录错误日志
     */
    protected open fun logError(
        e: Exception,
        builder: StringBuilder,
        startTime: Long,
        path: String
    ) {
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        val seconds = duration / 1000
        val millis = duration % 1000

        builder.apply {
            append("请求耗时>>> ${seconds}秒${millis}毫秒\n")
            append("网络请求异常>>> ${e.message}")
        }

        logFinal("ERROR", builder, path)
    }

    /**
     * 最终日志输出 - 子类可重写此方法实现自定义日志输出
     * @param builder 日志内容构建器
     * @param path 请求路径
     */
    protected open fun logFinal(tag: String, builder: StringBuilder, path: String) {
        var logTag = LogUtil.TAG_NET
        if (URLConstant.logNetFilter.contains(path)) {
            logTag = LogUtil.TAG_FILTER_NET
        }
        if (tag == "INFO") {
            LogUtil.i(
                logTag,
                builder.toString()
            )
        } else {
            LogUtil.e(
                logTag,
                builder.toString()
            )
        }
    }

    /**
     * 请求体转字符串
     */
    protected open fun bodyToString(request: RequestBody?): String? {
        return try {
            val buffer = Buffer()
            request?.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: IOException) {
            "无法读取请求体: ${e.message}"
        }
    }
}