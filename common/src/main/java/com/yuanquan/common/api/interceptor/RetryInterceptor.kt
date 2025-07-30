package com.yuanquan.common.api.interceptor

import com.yuanquan.common.utils.LogUtil
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 如果添加LoggingInterceptor拦截器，需要线添加RetryInterceptor后在添加LoggingInterceptor
 * 智能重试拦截器，支持基于重试次数的自定义时间间隔
 *
 * @param maxRetries 最大重试次数，默认3次
 * @param retryDelays 重试延迟时间列表（毫秒），默认[1000, 3000, 3000]
 * @param logTag 日志标签，默认"RetryInterceptor"
 * @param logEnabled 日志开关，默认开启
 * @param retryConditions 重试条件列表
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val retryDelays: List<Long> = listOf(1000, 3000, 3000),
    private val logTag: String = "RetryInterceptor",
    private val logEnabled: Boolean = false,
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

    init {
        // 验证延迟列表是否有效
        require(retryDelays.isNotEmpty()) { "重试延迟列表不能为空" }
        require(retryDelays.all { it >= 0 }) { "重试延迟时间不能为负数" }
    }

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

                // 获取当前重试的延迟时间
                val delay = getDelayForRetry(retryCount)

                if (logEnabled) {
                    LogUtil.w(logTag, "重试 #$retryCount: $method $url")
                    LogUtil.d(logTag, "使用延迟: ${delay}ms")
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

                // 获取当前重试的延迟时间
                val delay = getDelayForRetry(retryCount)

                if (logEnabled) {
                    LogUtil.w(logTag, "因网络错误重试 #$retryCount: $method $url")
                    LogUtil.d(logTag, "使用延迟: ${delay}ms")
                }

                TimeUnit.MILLISECONDS.sleep(delay)
            }
        }
    }

    /**
     * 根据重试次数获取延迟时间
     *
     * @param retryCount 当前重试次数（从1开始）
     * @return 延迟时间（毫秒）
     */
    private fun getDelayForRetry(retryCount: Int): Long {
        // 如果重试次数超过列表长度，使用最后一个延迟时间
        val index = (retryCount - 1).coerceAtMost(retryDelays.size - 1)
        return retryDelays[index]
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