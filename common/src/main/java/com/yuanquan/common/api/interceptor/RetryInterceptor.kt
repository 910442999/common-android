package com.yuanquan.common.api.interceptor

import com.yuanquan.common.BuildConfig
import com.yuanquan.common.api.URLConstant
import com.yuanquan.common.utils.LogUtil
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException
import kotlin.math.pow

/**
 * 智能重试拦截器，支持基于重试次数的自定义时间间隔
 * 增强功能：允许子类重写 logFinal 方法自定义日志输出
 *
 * 使用说明：
 * 1. 创建子类继承 RetryInterceptor
 * 2. 重写 logFinal 方法实现自定义日志输出
 * 3. 在拦截器链中使用自定义子类
 */
open class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val maxDelay: Long = 9000, // 最大9秒
    private val retryDelay: Long = 3000,
    private val retryableStatusCodes: Set<Int> = setOf(500, 502, 503, 504, 429),
    private val logEnabled: Boolean = false,
    private val retryPredicate: (Request, Response?, Throwable?) -> Boolean = { _, response, exception ->
        exception is IOException ||
                (response != null && response.code in retryableStatusCodes)
    }
) : Interceptor {

    // 重试计数器
    protected var totalRetryAttempts = 0

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val httpUrl = request.url
        val path = httpUrl.encodedPath
        val startTime = System.currentTimeMillis()

        // 重置计数器
        totalRetryAttempts = 0

        // 创建日志构建器
        val logBuilder = StringBuilder()
        if (logEnabled) {
            logBuilder.apply {
//                append("\n")
                if (BuildConfig.DEBUG) append("请求Headers>>> ${request.headers}\n")
                append("请求URL ${request.method} >>> $httpUrl\n")
//                append("API>>> $path\n")
                if (totalRetryAttempts != 0) {
                    append("最大重试次数>>> $maxRetries\n")
                    append("基础重试延迟>>> ${retryDelay}ms\n")
                }
                if (request.method == "POST" || request.method == "PUT") {
                    append("请求参数>>> ${bodyToString(request.body)}\n")
                }
//                append("初始请求时间>>> ${System.currentTimeMillis()}\n")
                logRetryAttempt(0, "初始请求", logBuilder)
            }
        }

        var response: Response? = null
        var exception: IOException? = null

        for (attempt in 0..maxRetries) {
            try {
                // 记录重试尝试（排除初始请求）
                if (attempt > 0) {
                    totalRetryAttempts++
                }
                response = chain.proceed(request)

                // 检查是否需要重试
                if (attempt < maxRetries && shouldRetry(request, response, null)) {
                    response.close()

                    // 计算延迟时间
                    val delay = calculateExponentialBackoffDelay(attempt, retryDelay)

                    // 获取重试原因
                    val reason = getRetryReason(response, null)

                    // 记录重试日志
                    if (logEnabled) {
                        logRetryAttempt(attempt + 1, reason, logBuilder, delay)
                    }

                    delayRetry(delay)
                    continue
                }

                // 记录成功响应日志
                logResponse(response, logBuilder, startTime, path)
                return response
            } catch (e: IOException) {
                exception = e
                // 记录重试尝试（排除初始请求）
                if (attempt > 0) {
                    totalRetryAttempts++
                }
                // 检查是否需要重试
                if (attempt < maxRetries && shouldRetry(request, null, e)) {
                    // 计算延迟时间
                    val delay = calculateExponentialBackoffDelay(attempt, retryDelay, e)

                    // 获取重试原因
                    val reason = getRetryReason(null, e)

                    // 记录重试日志
                    if (logEnabled) {
                        logRetryAttempt(attempt + 1, reason, logBuilder, delay)
                    }

                    delayRetry(delay)
                    continue
                }

                // 记录错误日志
                logError(e, logBuilder, startTime, path)
                throw e
            }
        }

        // 所有重试尝试都失败
        logError(
            exception ?: IOException("Unknown error after $maxRetries retries"),
            logBuilder, startTime, path
        )
        throw exception ?: IOException("Unknown error after $maxRetries retries")
    }

    /**
     * 获取详细的重试原因
     */
    protected open fun getRetryReason(response: Response?, exception: Throwable?): String {
        return when {
            exception != null -> {
                when (exception) {
                    is SocketTimeoutException -> "连接超时"
                    is ConnectException -> "连接失败"
                    is SSLHandshakeException -> "SSL握手失败"
                    else -> "网络异常: ${exception.javaClass.simpleName}"
                }
            }

            response != null -> {
                when (response.code) {
                    500 -> "服务器内部错误"
                    502 -> "网关错误"
                    503 -> "服务不可用"
                    504 -> "网关超时"
                    429 -> "请求过多"
                    else -> "HTTP状态码: ${response.code}"
                }
            }

            else -> "未知原因"
        }
    }

    /**
     * 判断是否需要重试
     */
    protected open fun shouldRetry(
        request: Request,
        response: Response?,
        exception: Throwable?
    ): Boolean {
        return retryPredicate(request, response, exception)
    }

    /**
     * 延迟重试
     */
    protected open fun delayRetry(delay: Long) {
        Thread.sleep(delay)
    }

    /**
     * 计算指数退避延迟
     */
    protected open fun calculateExponentialBackoffDelay(
        attempt: Int,
        baseDelay: Long
    ): Long {
        var exponentialDelay = (baseDelay * 2.0.pow(attempt.toDouble())).toLong()
        // 添加随机抖动（0.8 - 1.2倍）
        // 需要抖动：
        //避免多个客户端同步重试
        //减少"重试风暴"（Retry Storm）
        val jitter = 0.8 + (0.4 * Math.random()) // 0.8到1.2之间的随机数
        exponentialDelay = (exponentialDelay * jitter).toLong()
        // 设置最大延迟上限
        return minOf(exponentialDelay, maxDelay)
    }

    protected open fun calculateExponentialBackoffDelay(
        attempt: Int,
        baseDelay: Long,
        exception: Exception? = null
    ): Long {
        // 1. 计算基础指数延迟
        val baseExponent = when {
            exception is SocketTimeoutException -> 1.5  // 连接超时
            exception is ConnectException -> 2.0       // 连接失败
            exception is SSLHandshakeException -> 3.0  // SSL错误
            else -> 2.0                                // 默认
        }

        var delay = (baseDelay * baseExponent.pow(attempt.toDouble())).toLong()

        // 2. 添加随机抖动（0.8 - 1.2倍）
        // 需要抖动：
        //避免多个客户端同步重试
        //减少"重试风暴"（Retry Storm）
        val jitter = 0.8 + (0.4 * Math.random()) // 0.8到1.2之间的随机数
        delay = (delay * jitter).toLong()

        // 3. 设置最大延迟上限
        return minOf(delay, maxDelay)
    }

    /**
     * 记录重试尝试
     */
    protected open fun logRetryAttempt(
        attempt: Int,
        reason: String,
        builder: StringBuilder,
        delay: Long? = null
    ) {
        if (attempt != 0) {
            builder.apply {
//                append("\n")
                append("重试尝试 #$attempt\n")
                append("重试原因>>> $reason\n")
                if (delay != null) {
                    append("下次重试延迟>>> ${delay}ms\n")
                }
                append("重试时间>>> ${System.currentTimeMillis()}\n")
            }
        }
    }

    /**
     * 记录响应日志
     */
    protected open fun logResponse(
        response: Response?,
        builder: StringBuilder,
        startTime: Long,
        path: String
    ) {
        if (!logEnabled) return
        response?.let {
            // 创建响应副本，不影响原始响应流
            val responseBody = it.peekBody(Long.MAX_VALUE)
            val result = responseBody.string()
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            val seconds = duration / 1000
            val millis = duration % 1000

            builder.apply {
//                append("\n")
                append("最终响应状态码>>> ${it.code}   耗时>>> ${seconds}秒${millis}毫秒\n")
                if (totalRetryAttempts != 0) append("总重试次数>>> $totalRetryAttempts\n")
                append("响应结果>>> $result")
            }

            logFinal("INFO", builder, path)
        }
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
        if (!logEnabled) return
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        val seconds = duration / 1000
        val millis = duration % 1000

        builder.apply {
//            append("\n")
            append("最终错误>>> ${e.javaClass.simpleName}\n")
            append("错误信息>>> ${e.message}\n")
            append("总请求耗时>>> ${seconds}秒${millis}毫秒\n")
            append("总重试次数>>> $totalRetryAttempts")
        }

        logFinal("ERROR", builder, path)
    }

    /**
     * 最终日志输出 - 子类可重写此方法实现自定义日志输出
     * @param builder 日志内容构建器
     * @param path 请求路径
     */
    protected open fun logFinal(tag: String, builder: StringBuilder, path: String) {
        // 默认实现：根据路径过滤规则输出日志
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