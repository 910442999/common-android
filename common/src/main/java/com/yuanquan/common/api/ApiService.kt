package com.yuanquan.common.api

import com.yuanquan.common.api.response.BaseResult
import io.reactivex.rxjava3.core.Observable
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*


interface ApiService {

    /**
     * 注册
     */
    @POST("register")
    suspend fun register(@Body body: RequestBody): BaseResult<String>

    /**
     * 上传文件
     */
    @POST
    fun postUploadFile(
        @Url url: String,
        @Body body: RequestBody
    ): Observable<Any>

    /**
     * 上传文件
     */
    @PUT
    fun putUploadFile(
        @Url url: String,
        @Body body: RequestBody
    ): Observable<Any>

    @Streaming
    @GET
    fun downloadFile(@Url fileUrl: String): Observable<ResponseBody>
}