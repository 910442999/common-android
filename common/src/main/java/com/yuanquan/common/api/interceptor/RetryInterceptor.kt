package com.yuanquan.common.api.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class RetryInterceptor(
    private val maxRetries: Int = 3,          // 最大重试次数
    private val retryDelayMillis: Long = 1000, // 重试延迟时间（毫秒）
    private val retryConditions: List<(Response) -> Boolean> = listOf(
        // 默认重试条件：服务器错误（5xx）或网络错误
        { response -> response.code in 500..599 },
        { response -> response.code == 429 }   // 429 Too Many Requests
    )
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = doRequest(chain, request)
        var retryCount = 0

        while (shouldRetry(response, retryCount)) {
            retryCount++
            try {
                // 指数退避策略
                val delay = retryDelayMillis * (1 shl retryCount)
                TimeUnit.MILLISECONDS.sleep(delay)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw e
            }
            response = doRequest(chain, request)
        }

        return response ?: throw IOException("All retries failed")
    }

    private fun doRequest(chain: Interceptor.Chain, request: Request): Response? {
        return try {
            chain.proceed(request)
        } catch (e: IOException) {
            null // 网络异常时返回null
        }
    }

    private fun shouldRetry(response: Response?, retryCount: Int): Boolean {
        // 检查重试次数
        if (retryCount >= maxRetries) return false

        // 检查响应是否为空（网络异常）
        if (response == null) return true

        // 检查自定义重试条件
        return retryConditions.any { condition -> condition(response) }
    }
}