package com.yuanquan.common.api

import android.util.Log
import com.yuanquan.common.api.interceptor.LoggingInterceptor
import okhttp3.*
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

class SSEManager private constructor() {

    companion object {
        @Volatile
        private var instance: SSEManager? = null

        fun getInstance(): SSEManager = instance ?: synchronized(this) {
            instance ?: SSEManager().also { instance = it }
        }
    }

    private var eventSource: EventSource? = null
    private var sseListener: SSEEventListener? = null

    // 配置 OkHttpClient，注意 readTimeout 设置为 0 表示不超时，这对长连接很重要 [6,7](@ref)
    // OkHttpClient客户端
    private val okHttpClient: OkHttpClient by lazy { getOkHttpClientBuilder().build() }

    /**
     * OkHttpClient客户端
     */
    fun getOkHttpClientBuilder(): OkHttpClient.Builder = getOkHttpClientBuilder(90, 0)

    /**
     * OkHttpClient客户端
     */
    fun getOkHttpClientBuilder(timeout: Long, readTimeout: Long): OkHttpClient.Builder =
        getOkHttpClientBuilder(timeout, readTimeout, timeout)

    /**
     * OkHttpClient客户端
     */
    fun getOkHttpClientBuilder(
        timeout: Long, readTimeout: Long, writeTimeout: Long
    ): OkHttpClient.Builder = OkHttpClient.Builder().apply {
        connectTimeout(timeout, TimeUnit.SECONDS)// 连接时间：60s超时
        readTimeout(readTimeout, TimeUnit.SECONDS)// 读取时间：0s超时
        writeTimeout(writeTimeout, TimeUnit.SECONDS)// 写入时间：60s超时
        retryOnConnectionFailure(true) // 允许连接失败时重试
    }

    /**
     * 连接到 SSE 端点
     * @param url SSE 服务器地址
     * @param listener 事件监听器
     */
    fun connect(url: String, listener: SSEEventListener) {
        var headers = Headers.Builder()
        headers.add("Accept", "text/event-stream") // 声明接受 SSE 流 [6](@ref)
        headers.add("Cache-Control", "no-cache")
        headers.add("Connection", "keep-alive") // 建议：保持长连接
        this.connect(url, okHttpClient, headers.build(), listener)
    }

    fun connect(
        url: String, headers: Headers, listener: SSEEventListener
    ) {
        this.connect(url, okHttpClient, headers, listener)
    }

    fun connect(
        url: String, okHttpClient: OkHttpClient, headers: Headers, listener: SSEEventListener
    ) {
        this.sseListener = listener
        try {
            val request = Request.Builder().url(url).headers(headers).build()
            val factory = EventSources.createFactory(okHttpClient)
            eventSource = factory.newEventSource(request, object : EventSourceListener() {
                override fun onOpen(eventSource: EventSource, response: Response) {
                    Log.d("SSE", "连接已建立")
                    sseListener?.onOpen()
                    // 可以在这里更新 UI 或状态，例如通过 LiveData [5](@ref)
                }

                override fun onEvent(
                    eventSource: EventSource, id: String?, type: String?, data: String
                ) {
                    Log.d("SSE", "收到事件: id=$id, type=$type, data=$data")
                    // 处理接收到的数据 [1,6](@ref)
                    if (data != "[DONE]") { // 常见的流结束标记 [1](@ref)
                        sseListener?.onEvent(data)
                    } else {
                        Log.d("SSE", "流结束")
                        sseListener?.onComplete()
                    }
                }

                override fun onClosed(eventSource: EventSource) {
                    Log.d("SSE", "连接已关闭")
                    sseListener?.onClosed()
                }

                override fun onFailure(
                    eventSource: EventSource, t: Throwable?, response: Response?
                ) {
                    Log.e("SSE", "连接失败: ${t?.message}", t)
                    sseListener?.onFailure(t?.message ?: "Unknown error")
                    // 可以根据需要实现重连逻辑 [5,7](@ref)
                }
            })
        } catch (e: Exception) {
            Log.e("SSE", "连接异常: ${e.message}", e)
            sseListener?.onFailure(e.message ?: "Connection exception")
        }
    }

    /**
     * 关闭 SSE 连接
     * 在 Activity/Fragment 的 onDestroy 等生命周期方法中调用此方法很重要，以避免资源泄漏 [2](@ref)
     */
    fun disconnect() {
        eventSource?.cancel()
        eventSource = null
        Log.d("SSE", "连接已断开")
    }

    /**
     * 检查是否已连接
     */
    fun isConnected(): Boolean {
        return eventSource != null
    }
}

/**
 * SSE 事件监听接口，用于将事件回调给调用者
 */
interface SSEEventListener {
    fun onOpen()
    fun onEvent(data: String)
    fun onComplete()
    fun onClosed()
    fun onFailure(errorMessage: String)
}