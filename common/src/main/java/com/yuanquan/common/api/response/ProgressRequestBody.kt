package com.yuanquan.common.api.response

import com.yuanquan.common.interfaces.ProgressListener
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer
import java.io.IOException

/**
* 带进度监听和 MD5 计算的请求体
* 优化版本：使用单个 ProgressListener 接口处理所有回调
*
* @param requestBody 原始请求体
* @param progressListener 进度和 MD5 监听器
*/
class ProgressRequestBody(
    private val requestBody: RequestBody,
    private val progressListener: ProgressListener?,
    private val isCancelled: () -> Boolean = { false } // 取消检查函数
) : RequestBody() {
    private var currentBytesWritten: Long = 0
    private var totalContentLength: Long = 0
    private var lastReportedProgress = -1

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return requestBody.contentLength()
    }

    override fun contentType(): MediaType? {
        return requestBody.contentType()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        totalContentLength = contentLength()

        // 创建带进度监听和 MD5 计算的 Sink
        val progressSink = object : ForwardingSink(sink) {
            @Throws(IOException::class)
            override fun write(source: Buffer, byteCount: Long) {
                // 检查是否已取消
                if (isCancelled()) {
                    throw IOException("Upload cancelled by user")
                }

                // 分块处理大文件，避免内存占用过高
                val bufferSize = 8192
                var remaining = byteCount

                while (remaining > 0) {
                    val chunkSize = minOf(remaining, bufferSize.toLong())
                    val chunkBuffer = ByteArray(chunkSize.toInt())

                    source.read(chunkBuffer)
                    super.write(Buffer().write(chunkBuffer), chunkSize)

                    // 更新进度
                    currentBytesWritten += chunkSize
                    remaining -= chunkSize

                    notifyProgress()
                }
            }

            private fun notifyProgress() {
                progressListener?.let {
                    if (totalContentLength > 0) {
                        // 计算进度百分比
                        val progress = ((currentBytesWritten * 100.0) / totalContentLength).toInt()
                        if (progress != lastReportedProgress) {
                            lastReportedProgress = progress
                            it.onProgress(progress)
                        }
                    } else {
                        // 总长度未知时回调特殊值
                        it.onProgress(0)
                    }
                }
            }
        }.buffer()

        // 使用我们自定义的 Sink 写入数据
        requestBody.writeTo(progressSink)
        progressSink.flush()
    }
}