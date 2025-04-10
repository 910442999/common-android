package com.yuanquan.common.api

import kotlinx.coroutines.*
import retrofit2.Response

/**
 * 一、使用示例
 * 1. 定义 Retrofit 接口（示例）
 * interface ApiService {
 *     @GET("user/{id}")
 *     suspend fun getUser(@Path("id") id: String): Response<User>
 *
 *     @GET("news/latest")
 *     suspend fun getLatestNews(): Response<News>
 *
 *     @GET("weather")
 *     suspend fun getWeather(): Response<Weather>
 * }
 *
 * 2. 并发请求多个接口
 *
 * // 在 ViewModel 或 Activity 中调用
 * viewModelScope.launch {
 *     val requests = listOf(
 *         { apiService.getUser("123") },
 *         { apiService.getLatestNews() },
 *         { apiService.getWeather() }
 *     )
 *
 *     val results = CoroutineRequestUtils.concurrentRequests(requests)
 *
 *     results.forEach { result ->
 *         when (result) {
 *             is Result.Success -> {
 *                 // 处理成功数据
 *                 when (result.value) {
 *                     is User -> { /* 更新用户数据 */ }
 *                     is News -> { /* 更新新闻数据 */ }
 *                     is Weather -> { /* 更新天气数据 */ }
 *                 }
 *             }
 *             is Result.Failure -> {
 *                 // 处理失败
 *                 Log.e("TAG", "请求失败: ${result.exception.message}")
 *             }
 *         }
 *     }
 * }
 *
 * 3. 同步请求多个接口（逐个执行）
 *
 * viewModelScope.launch {
 *     val requests = listOf(
 *         { apiService.getUser("123") },
 *         { apiService.getLatestNews() },
 *         { apiService.getWeather() }
 *     )
 *
 *     val results = CoroutineRequestUtils.sequentialRequests(requests)
 *     // 处理结果同上
 * }
 *
 * 二、扩展优化
 *
 * 结果过滤：
 * // 只保留成功的结果
 * val successResults = results.filterIsInstance<Result.Success<T>>()
 *
 * 结合 Flow 使用：
 * fun <T> concurrentRequestsFlow(requests: List<suspend () -> Response<T>>) = flow {
 *     emit(CoroutineRequestUtils.concurrentRequests(requests))
 * }
 *
 *三、依赖配置（build.gradle）
 * dependencies {
 *     // 协程核心库
 *     implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3"
 *     implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
 *
 *     // Retrofit（网络请求示例）
 *     implementation "com.squareup.retrofit2:retrofit:2.9.0"
 *     implementation "com.squareup.retrofit2:converter-gson:2.9.0"
 * }
 *
 */
object CoroutineRequestUtils {

    // 并发请求多个接口（同时执行，等待所有结果）
    suspend fun <T> concurrentRequests(
        requests: List<suspend () -> Response<T>>,
        timeoutMs: Long = 60000
    ): List<Result<T>> = coroutineScope {
        requests.map {
            async {
                withTimeout(timeoutMs) { safeRequest(it) }
            }
        }.awaitAll()
    }

    // 同步请求多个接口（按顺序逐个执行）
    suspend fun <T> sequentialRequests(
        requests: List<suspend () -> Response<T>>
    ): List<Result<T>> {
        return requests.map { safeRequest(it) }
    }

    // 安全处理请求（捕获异常）
    suspend fun <T> safeRequest(
        block: suspend () -> Response<T>
    ): Result<T> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}