package com.yuanquan.common.api.interceptor

import com.yuanquan.common.utils.LogUtil
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 如果添加LoggingInterceptor拦截器，需要线添加RetryInterceptor后在添加LoggingInterceptor
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val retryDelayMillis: Long = 1000,
    private val logTag: String = "RetryInterceptor",
    private val logEnabled: Boolean = true,
    private val retryConditions: List<(Response) -> Boolean> = listOf(
        { response ->
            val shouldRetry = response.code in 500..599
            if (shouldRetry && logEnabled) {
                LogUtil.d(logTag, "重试条件: 服务器错误 (${response.code})")
            }
            shouldRetry
        },
        { response ->
            val shouldRetry = response.code == 429
            if (shouldRetry && logEnabled) {
                LogUtil.d(logTag, "重试条件: 请求过多 (429)")
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
            LogUtil.d(logTag, "开始请求: $method $url")
        }

        var response: Response? = null
        var retryCount = 0

        while (true) {
            // 关闭前一次响应（如果存在）
            response?.close()

            try {
                response = chain.proceed(request)

                if (logEnabled) {
                    val status = if (response.isSuccessful) "成功" else "失败"
                    LogUtil.d(logTag, "响应: $method $url - ${response.code} ($status)")
                }

                // 检查是否需要重试
                if (!shouldRetry(response, retryCount, url, method)) {
                    return response
                }

                retryCount++

                if (logEnabled) {
                    LogUtil.w(logTag, "重试 #$retryCount: $method $url")
                }

                // 指数退避策略
                val delay = retryDelayMillis * (1 shl retryCount)

                if (logEnabled) {
                    LogUtil.d(logTag, "等待 ${delay}ms 后重试...")
                }

                TimeUnit.MILLISECONDS.sleep(delay)

                if (logEnabled) {
                    LogUtil.d(logTag, "开始重试 #$retryCount: $method $url")
                }

            } catch (e: IOException) {
                if (logEnabled) {
                    LogUtil.e(logTag, "网络错误: $method $url - ${e.message}")
                }

                // 检查是否需要重试网络错误
                if (!shouldRetry(null, retryCount, url, method)) {
                    throw e
                }

                retryCount++

                if (logEnabled) {
                    LogUtil.w(logTag, "因网络错误重试 #$retryCount: $method $url")
                }

                // 指数退避策略
                val delay = retryDelayMillis * (1 shl retryCount)
                TimeUnit.MILLISECONDS.sleep(delay)
            }
        }
    }

    private fun shouldRetry(
        response: Response?,
        retryCount: Int,
        url: String,
        method: String
    ): Boolean {
        if (retryCount >= maxRetries) {
            if (logEnabled) {
                LogUtil.w(logTag, "达到最大重试次数 ($maxRetries): $method $url")
            }
            return false
        }

        return when {
            // 网络错误情况
            response == null -> {
                if (logEnabled) {
                    LogUtil.d(logTag, "因网络错误重试: $method $url")
                }
                true
            }
            // 检查自定义重试条件
            retryConditions.any { it(response) } -> {
                if (logEnabled) {
                    LogUtil.d(logTag, "满足重试条件: $method $url (状态码: ${response.code})")
                }
                true
            }
            else -> {
                if (logEnabled) {
                    LogUtil.d(logTag, "不满足重试条件: $method $url (状态码: ${response.code})")
                }
                false
            }
        }
    }
}