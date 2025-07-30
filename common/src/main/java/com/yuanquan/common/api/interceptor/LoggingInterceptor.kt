package com.yuanquan.common.api.interceptor

import com.yuanquan.common.api.URLConstant
import com.yuanquan.common.utils.LogUtil
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import java.io.IOException
/**
 * 如果添加RetryInterceptor拦截器，需要线添加RetryInterceptor后在添加LoggingInterceptor
 */
class LoggingInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val httpUrl = request.url
        val startTime = System.currentTimeMillis()
        //        if (httpUrl.toString().contains(".png") || httpUrl.toString()
//                .contains(".jpg") || httpUrl.toString().contains(".jpeg") || httpUrl.toString()
//                .contains(".gif")
//        ) {
//            return chain.proceed(request)
//        }
        val path = httpUrl.encodedPath

        val builder = StringBuilder().apply {
            append("\n")
            append("请求Headers>>> ${request.headers}\n")
            append("请求URL>>> $httpUrl\n")
            append("API>>> $path\n")
            append("请求方法>>> ${request.method}\n")

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

    private fun logResponse(
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
            append("响应状态码>>> ${response.code}\n")
            append("请求耗时>>> ${seconds}秒${millis}毫秒\n")
            append("响应结果>>> $result\n")
        }

        logFinal(builder, path)
    }

    private fun logError(
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
            append("网络请求异常>>> ${e.message}\n")
        }

        logFinal(builder, path)
    }

    private fun logFinal(builder: StringBuilder, path: String) {
        if (URLConstant.logNetFilter.contains(path)) {
            LogUtil.i(LogUtil.TAG_FILTER_NET, builder.toString())
        } else {
            LogUtil.i(LogUtil.TAG_NET, builder.toString())
        }
    }

    private fun bodyToString(request: RequestBody?): String? {
        return try {
            val buffer = Buffer()
            request?.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: IOException) {
            "无法读取请求体: ${e.message}"
        }
    }
}