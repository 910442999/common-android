package com.yuanquan.common.api.retrofit

import com.yuanquan.common.BuildConfig
import com.yuanquan.common.api.URLConstant
import com.yuanquan.common.api.gson.GsonConverterFactory
import com.yuanquan.common.api.interceptor.LoggingInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
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
     * 创建API Service接口实例
     */
    fun <T> create(baseUrl: String, clazz: Class<T>): T =
        Retrofit.Builder().baseUrl(baseUrl).client(mClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(clazz)

    /**
     * 创建API Service接口实例
     */
    fun <T> create(baseUrl: String, client: OkHttpClient, clazz: Class<T>): T =
        Retrofit.Builder().baseUrl(baseUrl).client(client)
            .addConverterFactory(GsonConverterFactory.create())
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