package com.yuanquan.common.api.retrofit

import com.yuanquan.common.BuildConfig
import com.yuanquan.common.api.interceptor.LoggingInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 接口请求工厂
 * @author ssq
 */
class RetrofitClient {

    companion object {
        @Volatile
        private var retrofitClient: RetrofitClient? = null
        fun getInstance() = retrofitClient ?: synchronized(this) {
            retrofitClient ?: RetrofitClient().also {
                retrofitClient = it
            }
        }
    }
//    private var cookieJar: PersistentCookieJar = PersistentCookieJar(
//        SetCookieCache(),
//        SharedPrefsCookiePersistor(App.instance)
//    )

    // 日志拦截器
    private val mLoggingInterceptor: Interceptor by lazy {
        LoggingInterceptor()
    }

    // OkHttpClient客户端
    private val mClient: OkHttpClient by lazy { getOkHttpClientBuilder().build() }

    /**
     * 创建API Service接口实例(全部默认)
     */
    fun <T> create(baseUrl: String, clazz: Class<T>): T =
        create(baseUrl, mClient, GsonConverterFactory.create(), clazz)

    /**
     * 创建API Service接口实例（全部使用自定义）
     */
    fun <T> create(
        baseUrl: String,
        client: OkHttpClient,
        factory: Converter.Factory,
        clazz: Class<T>
    ): T =
        Retrofit.Builder().baseUrl(baseUrl).client(client)
            .addConverterFactory(factory)
            .build()
            .create(clazz)

    /**
     * OkHttpClient客户端
     */
    fun getOkHttpClientBuilder(): OkHttpClient.Builder = OkHttpClient.Builder().apply {
        connectTimeout(60, TimeUnit.SECONDS)// 连接时间：30s超时
        readTimeout(60, TimeUnit.SECONDS)// 读取时间：10s超时
        writeTimeout(60, TimeUnit.SECONDS)// 写入时间：10s超时
        if (BuildConfig.DEBUG) addInterceptor(mLoggingInterceptor)// 仅debug模式启用日志过滤器
    }
}