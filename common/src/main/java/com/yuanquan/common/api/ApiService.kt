package com.yuanquan.common.api

import com.yuanquan.common.api.response.BaseResult
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*


interface ApiService {

    /**
     * 注册
     */
    @POST("oauth-server/app/account/v1/register")
    suspend fun register(@Body body: RequestBody): BaseResult<String>


}