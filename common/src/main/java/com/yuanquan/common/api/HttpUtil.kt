package com.yuanquan.common.api

import com.yuanquan.common.api.response.ProgressRequestBody
import com.yuanquan.common.api.retrofit.RetrofitClient
import com.yuanquan.common.interfaces.OnDownloadListener
import com.yuanquan.common.interfaces.OnUpdateListener
import com.yuanquan.common.interfaces.ProgressListener
import com.yuanquan.common.utils.LogUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.exceptions.CompositeException
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSink
import okio.Okio
import okio.Source
import okio.source
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeoutException
import okio.buffer
import okio.source

class HttpUtil {
    private var uploadDisposable: Disposable? = null
    private var downloadDisposable: Disposable? = null

    private val mService by lazy {
        RetrofitClient.getInstance().create(URLConstant.getHost(), ApiService::class.java)
    }

    //suspend fun test(options: LinkedHashMap<String, String?>) = mService.test(options)

//    suspend fun verificationPhone(page: RequestBody) = mService.verificationPhone(page)


    companion object {
        @Volatile
        private var httpUtil: HttpUtil? = null

        @JvmStatic
        fun getInstance() = httpUtil ?: synchronized(this) {
            httpUtil ?: HttpUtil().also { httpUtil = it }
        }
    }

    //可以直接在BaseViewModel中获取取ApiService对象，简化接口调用
    fun getService(): ApiService {
        return mService
    }

    /**
     * 生成上传请求的文件参数
     * @param file 上传文件
     */
    fun getMultipartBody(file: File, name: String = "file"): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            name, file.name, file.asRequestBody(null)
        )
    }

    fun getMultipartBody(
        elements: ByteArray,
        name: String = "file",
        fileName: String = "fileName"
    ): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            name, fileName, RequestBody.create(null, elements)
        )
    }

    fun getRequestBody(json: String): RequestBody {
        return json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    }

    fun getRequestBody(file: File?): RequestBody {
        if (file == null) throw NullPointerException("file == null")
        return file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
    }

    fun getRequestBody(file: File?, progressListener: ProgressListener): ProgressRequestBody {
        val requestBody = ProgressRequestBody(this.getRequestBody(file), progressListener)
        return requestBody
    }

    /**
     * 处理大文件流
     * Returns a new request body that transmits the content of `file`.
     */
    fun getRequestBody(
        inputStream: InputStream?,
        contentType: String = "application/octet-stream"
    ): RequestBody {
        if (inputStream == null) throw NullPointerException("inputStream == null")
        return object : RequestBody() {
            private val mediaType: MediaType = contentType.toMediaTypeOrNull()
                ?: "application/octet-stream".toMediaTypeOrNull()!!

            override fun contentType() = mediaType

            override fun contentLength(): Long = -1 // 使用分块传输

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                inputStream.source().buffer().use { source ->
                    sink.writeAll(source)
                }
            }
        }
    }

    fun uploadFile(
        url: String, body: MultipartBody.Part, listener: OnUpdateListener
    ) {
        var uploadFile = mService.uploadFile(url = url, file = body)
        uploadFile.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Any> { // 替换为实际类型
                override fun onSubscribe(d: Disposable) {
                    uploadDisposable = d
                    listener.onStart()
                }

                override fun onNext(url: Any) {
                    listener.onComplete(url) // 假设此处有最终URL
                }

                override fun onError(e: Throwable) {
                    val isCanceled = when {
                        e is CompositeException -> e.exceptions.any(::isCancelCause)
                        else -> isCancelCause(e)
                    }
                    if (isCanceled) {
                        LogUtil.e("上传已取消")
                        listener.onDisposed(e.message)
                    } else {
                        LogUtil.e("文件连接失败 :${e.message}")
                        listener.onError(e.message)
                    }
                    cancelUpload()
                }

                override fun onComplete() {
                    cancelUpload()
                }

                private fun isCancelCause(e: Throwable): Boolean {
                    return e is InterruptedIOException
                            || e.message?.contains("disposed") == true
//                            || e is DisposedException
                            || e.cause?.let(::isCancelCause) == true
                }
            })
    }

    /**
     * 上传文件
     */
    fun uploadFile(
        url: String, body: RequestBody, listener: OnUpdateListener, method: String = "POST"
    ) {
        var uploadFile: Observable<Any>
        if (method == "PUT") {
            uploadFile = mService.putUploadFile(
                url = url, body = ProgressRequestBody(
                    body
                ) { progress ->
                    //更新UI需切换到UI线程
                    listener.onProgress(progress)
                }
            )
        } else {
            uploadFile = mService.postUploadFile(
                url = url, body = ProgressRequestBody(
                    body
                ) { progress ->
                    //更新UI需切换到UI线程
                    listener.onProgress(progress)
                }
            )
        }
        uploadFile.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Any> { // 替换为实际类型
                override fun onSubscribe(d: Disposable) {
                    uploadDisposable = d
                    listener.onStart()
                }

                override fun onNext(url: Any) {
                    listener.onComplete(url) // 假设此处有最终URL
                }

                override fun onError(e: Throwable) {
                    val isCanceled = when {
                        e is CompositeException -> e.exceptions.any(::isCancelCause)
                        else -> isCancelCause(e)
                    }
                    if (isCanceled) {
                        LogUtil.e("上传已取消")
                        listener.onDisposed(e.message)
                    } else {
                        LogUtil.e("文件连接失败 :${e.message}")
                        listener.onError(e.message)
                    }
                    cancelUpload()
                }

                override fun onComplete() {
                    cancelUpload()
                }

                private fun isCancelCause(e: Throwable): Boolean {
                    return e is InterruptedIOException
                            || e.message?.contains("disposed") == true
//                            || e is DisposedException
                            || e.cause?.let(::isCancelCause) == true
                }
            })
    }

    // 取消方法
    fun cancelUpload() {
        uploadDisposable?.apply {
            if (!isDisposed) {
                dispose()
                uploadDisposable = null
                LogUtil.e("上传结束")
            }
        }
    }

    // 文件下载方法（ViewModel/Presenter层）
    fun downloadFile(
        fileUrl: String, outputFile: File, progressListener: OnDownloadListener? = null
    ) {
        mService.downloadFile(fileUrl).subscribeOn(Schedulers.io()) // 确保网络请求在IO线程
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { responseBody ->
                saveToFile(responseBody, outputFile, progressListener)
            }// 结果回到主线程
            .subscribe(object : Observer<File> {
                override fun onSubscribe(d: Disposable) {
                    downloadDisposable = d
                    progressListener?.onStart()
                }

                override fun onNext(file: File) {
                    progressListener?.onComplete(file)
                }

                override fun onError(e: Throwable) {
                    handleDownloadError(e, progressListener)
                    cancelDownload()
                }

                override fun onComplete() {
                    cancelDownload()
                }
            })
    }

    // 错误处理
    private fun handleDownloadError(
        e: Throwable, progressListener: OnDownloadListener? = null
    ) {
        when {
            e is InterruptedIOException || e.message?.contains("disposed") == true -> {
                LogUtil.e("下载已取消")
                progressListener?.onCancel()
            }

            e is SocketTimeoutException -> {
                LogUtil.e("连接超时")
                progressListener?.onError(TimeoutException())
            }

            e is UnknownHostException -> {
                LogUtil.e("连接超时")
                progressListener?.onError(UnknownHostException())
            }

            else -> {
                LogUtil.e("文件下载失败: ${e.message}")
                progressListener?.onError(e)
            }
        }
    }

    // 取消方法
    fun cancelDownload() {
        downloadDisposable?.apply {
            if (!isDisposed) {
                dispose()
                downloadDisposable = null
                LogUtil.e("下载结束")
            }
        }
    }

    private fun saveToFile(
        body: ResponseBody,
        outputFile: File,
        listener: OnDownloadListener?
    ): Observable<File> {
        var lastReportedProgress = -1
        return Observable.create { emitter ->
            try {
                outputFile.parentFile?.mkdirs()
                var inputStream: InputStream? = null
                var outputStream: FileOutputStream? = null
                val totalBytes = body.contentLength()
                var writtenBytes = 0L
                try {
                    inputStream = body.byteStream()
                    outputStream = FileOutputStream(outputFile)
                    val buffer = ByteArray(4096)
                    var read: Int

                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                        writtenBytes += read
                        // 计算进度百分比，使用浮点避免整数溢出
                        val progress: Int = ((writtenBytes * 100.0) / totalBytes).toInt()
                        if (progress != lastReportedProgress) {
                            lastReportedProgress = progress
                            listener?.onProgress(progress, writtenBytes, totalBytes)
                        }
                    }

                    outputStream.flush()
                    emitter.onNext(outputFile)
                    emitter.onComplete()
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                }
            } catch (e: Exception) {
                outputFile.delete()
                emitter.onError(e)
            }
        }.subscribeOn(Schedulers.io())
    }

}