package com.yuanquan.common.api

import com.yuanquan.common.api.response.BaseResult
import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody
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

    @POST
    @Multipart // 必须使用 Multipart 注解
    fun uploadFile(
        @Url url: String,
        @Part file: MultipartBody.Part
    ): Observable<Any> // 根据实际返回类型替换 Any

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