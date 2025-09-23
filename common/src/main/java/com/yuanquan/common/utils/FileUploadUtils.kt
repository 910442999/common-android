package com.yuanquan.common.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.yuanquan.common.interfaces.ProgressListener
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.BufferedSink
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 文件上传工具类
 * 封装文件选择和上传功能
 */
class FileUploadUtils {

    // 上传回调接口
    interface UploadCallback {
        fun onProgress(progress: Int)
        fun onSuccess(fileUrl: String)
        fun onError(message: String)
        fun onCancelled()
    }

    companion object {
        // 单例实例
        @Volatile
        private var instance: FileUploadUtils? = null

        /**
         * 获取单例实例
         */
        fun getInstance(): FileUploadUtils {
            return instance ?: synchronized(this) {
                instance ?: FileUploadUtils().also { instance = it }
            }
        }
    }

    private var currentUploadCall: Call? = null
    private var isUploadCancelled = false
    private var lastReportedProgress = -1

    /**
     * 上传文件
     */
    fun uploadFile(
        context: Context,
        fileUri: Uri,
        uploadUrl: String,
        callback: UploadCallback
    ) {
        // 重置取消标志
        isUploadCancelled = false

        try {
            // 创建请求体
            val requestBody = this.createRequestBody(context, fileUri, object : ProgressListener {
                override fun onProgress(progress: Int) {
                    callback.onProgress(progress)
                }
            })
            // 执行上传
            executeUpload(requestBody, uploadUrl, callback)
        } catch (e: Exception) {
            callback.onError("上传准备失败: ${e.message}")
        }
    }

    fun uploadFile(
        requestBody: RequestBody,
        uploadUrl: String,
        callback: UploadCallback
    ) {
        // 重置取消标志
        isUploadCancelled = false
        try {
            // 执行上传
            executeUpload(requestBody, uploadUrl, callback)
        } catch (e: Exception) {
            callback.onError("上传准备失败: ${e.message}")
        }
    }

    /**
     * 取消上传
     */
    fun cancelUpload() {
        isUploadCancelled = true
        currentUploadCall?.cancel()
    }

    /**
     * 创建请求体
     */
    fun createRequestBody(context: Context, uri: Uri, callback: ProgressListener): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType? {
                return context.contentResolver.getType(uri)?.toMediaTypeOrNull()
            }

            override fun contentLength(): Long {
                return FileUtils.getFileSize(context, uri) ?: -1
            }

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                val contentResolver: ContentResolver = context.contentResolver
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bufferSize = calculateBufferSize(contentLength())
                    val buffer = ByteArray(bufferSize)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        // 检查是否已取消
                        if (isUploadCancelled) {
                            throw IOException("Upload cancelled by user")
                        }

                        if (bytesRead > 0) {
                            // 写入输出流
                            sink.write(buffer, 0, bytesRead)

                            // 更新进度
                            totalBytesRead += bytesRead
                            val progress = calculateProgress(totalBytesRead, contentLength())
                            // 回调进度

                            if (progress != lastReportedProgress) {
                                lastReportedProgress = progress
                                callback.onProgress(progress)
                            }
                            // 添加资源释放延迟
                            if (totalBytesRead % (10 * 1024 * 1024) == 0L) {
                                Thread.sleep(5)
                            }
                        }
                    }
                }
            }

            /**
             * 计算缓冲区大小
             */
            private fun calculateBufferSize(fileSize: Long): Int {
                return when {
                    fileSize > 500 * 1024 * 1024 -> 4 * 1024 // 4KB for >500MB
                    fileSize > 100 * 1024 * 1024 -> 8 * 1024 // 8KB for >100MB
                    fileSize > 50 * 1024 * 1024 -> 16 * 1024 // 16KB for >50MB
                    fileSize > 10 * 1024 * 1024 -> 32 * 1024 // 32KB for >10MB
                    else -> 64 * 1024 // 64KB for smaller files
                }
            }

            /**
             * 计算上传进度
             */
            private fun calculateProgress(bytesWritten: Long, totalBytes: Long): Int {
                return if (totalBytes > 0) {
                    ((bytesWritten * 100.0) / totalBytes).toInt()
                } else {
                    0
                }
            }
        }
    }

    /**
     * 执行上传
     */
    private fun executeUpload(
        requestBody: RequestBody,
        uploadUrl: String,
        callback: UploadCallback
    ) {
        // 创建OkHttpClient
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        // 创建请求
        val request = Request.Builder()
            .url(uploadUrl)
            .put(requestBody)
            .build()

        // 执行请求
        currentUploadCall = client.newCall(request)
        currentUploadCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (isUploadCancelled) {
                    callback.onCancelled()
                } else {
                    callback.onError("上传失败: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (isUploadCancelled) {
                    callback.onCancelled()
                } else if (response.isSuccessful) {
                    val fileUrl = parseResponse(response.body)
                    callback.onSuccess(fileUrl)
                } else {
                    callback.onError("上传失败: ${response.code}")
                }
            }

            /**
             * 解析响应
             */
            private fun parseResponse(responseBody: ResponseBody?): String {
                // 这里需要根据您的API响应格式解析
                // 示例：假设响应格式为 {"data": {"url": "https://example.com/file.jpg"}}
                return responseBody?.string() ?: ""
            }
        })
    }
}