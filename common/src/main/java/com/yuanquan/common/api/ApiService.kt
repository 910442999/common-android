package com.yuanquan.common.api

import com.yuanquan.common.api.response.BaseResult
import okhttp3.RequestBody
import retrofit2.http.*


interface ApiService {

    /**
     * 注册
     */
    @POST("register")
    suspend fun register(@Body body: RequestBody): BaseResult<String>


}