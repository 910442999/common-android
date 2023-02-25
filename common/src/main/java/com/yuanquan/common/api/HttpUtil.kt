package com.yuanquan.common.api

import com.yuanquan.common.api.retrofit.RetrofitClient
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.internal.Util
import okio.BufferedSink
import okio.Okio
import okio.Source
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*

class HttpUtil {
    private val mService by lazy {
        RetrofitClient.getInstance().create(URLConstant.getHost(), ApiService::class.java)
    }

    //suspend fun test(options: LinkedHashMap<String, String?>) = mService.test(options)

//    suspend fun verificationPhone(page: RequestBody) = mService.verificationPhone(page)


    companion object {
        @Volatile
        private var httpUtil: HttpUtil? = null

        fun getInstance() = httpUtil ?: synchronized(this) {
            httpUtil ?: HttpUtil().also { httpUtil = it }
        }
    }

    //可以直接在BaseViewModel中获取取ApiService对象，简化接口调用
    fun getService(): ApiService {
        return mService
    }

    /**
     * 生成上传图片请求的文件参数
     * @param file 上传文件
     */
    fun getUploadImageFile(file: File): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            "fileName",
            file.name, RequestBody.create(null, file)
        )
    }

    fun getRequestBody(json: String): RequestBody {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
    }

    fun getRequestBody(file: File?): RequestBody {
        if (file == null) throw NullPointerException("file == null")
        return RequestBody.create(MediaType.parse("application/octet-stream"), file)
    }

    /**
     * Returns a new request body that transmits the content of `file`.
     */
    fun getRequestBody(inputStream: InputStream?): RequestBody {
        if (inputStream == null) throw NullPointerException("inputStream == null")
        return object : RequestBody() {
            override fun contentType(): MediaType {
                return MediaType.parse("application/octet-stream")!!
            }

            override fun contentLength(): Long {
                return try {
                    inputStream.available().toLong()
                } catch (e: IOException) {
                    0
                }
            }

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                var source: Source? = null
                try {
                    source = Okio.source(inputStream)
                    sink.writeAll(source)
                } finally {
                    Util.closeQuietly(source)
                }
            }
        }
    }
}
