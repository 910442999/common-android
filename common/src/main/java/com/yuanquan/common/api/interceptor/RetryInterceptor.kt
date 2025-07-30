package com.yuanquan.common.api.interceptor

import com.yuanquan.common.utils.LogUtil
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class RetryInterceptor(
    private val maxRetries: Int = 3,          // 最大重试次数
    private val retryDelayMillis: Long = 1000, // 重试延迟时间（毫秒）
    private val logTag: String = "RetryInterceptor", // 日志标签
    private val logEnabled: Boolean = true,   // 日志开关
    private val retryConditions: List<(Response) -> Boolean> = listOf(
        // 默认重试条件：服务器错误（5xx）或网络错误
        { response ->
            val shouldRetry = response.code in 500..599
            if (shouldRetry && logEnabled) {
                LogUtil.d(logTag, "Retry condition: Server error (${response.code})")
            }
            shouldRetry
        },
        { response ->
            val shouldRetry = response.code == 429
            if (shouldRetry && logEnabled) {
                LogUtil.d(logTag, "Retry condition: Rate limited (429)")
            }
            shouldRetry
        }
    )
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val method = request.method

        if (logEnabled) {
            LogUtil.d(logTag, "Starting request: $method $url")
        }

        var response = doRequest(chain, request, url, method)
        var retryCount = 0

        while (shouldRetry(response, retryCount, url, method)) {
            retryCount++

            if (logEnabled) {
                LogUtil.w(logTag, "Retry #$retryCount for $method $url")
            }

            try {
                // 指数退避策略
                val delay = retryDelayMillis * (1 shl retryCount)

                if (logEnabled) {
                    LogUtil.d(logTag, "Waiting ${delay}ms before retry...")
                }

                TimeUnit.MILLISECONDS.sleep(delay)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                if (logEnabled) {
                    LogUtil.e(logTag, "Retry interrupted for $method $url", e)
                }
                throw e
            }

            if (logEnabled) {
                LogUtil.d(logTag, "Attempting retry #$retryCount for $method $url")
            }

            response = doRequest(chain, request, url, method)
        }

        if (retryCount > 0) {
            if (response != null && response.isSuccessful) {
                if (logEnabled) {
                    LogUtil.i(logTag, "Request succeeded after $retryCount retries: $method $url")
                }
            } else {
                if (logEnabled) {
                    LogUtil.e(logTag, "Request failed after $retryCount retries: $method $url")
                }
            }
        }

        return response ?: throw IOException("All retries failed for $method $url")
    }

    private fun doRequest(
        chain: Interceptor.Chain,
        request: Request,
        url: String,
        method: String
    ): Response? {
        return try {
            val response = chain.proceed(request)

            if (logEnabled) {
                val logMsg = "Response for $method $url: ${response.code}"
                if (response.isSuccessful) {
                    LogUtil.d(logTag, logMsg)
                } else {
                    LogUtil.w(logTag, logMsg)
                }
            }

            response
        } catch (e: IOException) {
            if (logEnabled) {
                LogUtil.e(logTag, "Network error for $method $url: ${e.message}")
            }
            null // 网络异常时返回null
        }
    }

    private fun shouldRetry(
        response: Response?,
        retryCount: Int,
        url: String,
        method: String
    ): Boolean {
        // 检查重试次数
        if (retryCount >= maxRetries) {
            if (logEnabled) {
                LogUtil.w(logTag, "Max retries reached ($maxRetries) for $method $url")
            }
            return false
        }

        // 检查响应是否为空（网络异常）
        if (response == null) {
            if (logEnabled) {
                LogUtil.d(logTag, "Retrying due to network error for $method $url")
            }
            return true
        }

        // 检查自定义重试条件
        val shouldRetry = retryConditions.any { condition -> condition(response) }

        if (!shouldRetry && logEnabled) {
            LogUtil.d(logTag, "No retry conditions met for $method $url (code: ${response.code})")
        }

        return shouldRetry
    }
}