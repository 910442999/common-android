package com.yuanquan.common.api.interceptor

import com.yuanquan.common.api.URLConstant
import com.yuanquan.common.utils.LogUtil
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import java.io.IOException


class LoggingInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val httpUrl = request.url()
        val t1 = System.nanoTime() //请求发起的时间

//        if (httpUrl.toString().contains(".png") || httpUrl.toString()
//                .contains(".jpg") || httpUrl.toString().contains(".jpeg") || httpUrl.toString()
//                .contains(".gif")
//        ) {
//            return chain.proceed(request)
//        }
        val builder = StringBuilder()
        var api = httpUrl.toString().replace(URLConstant.getHost(), "")
        if (api.contains("?")) {
            api = api.substring(0, api.indexOf("?"))
        }
        builder.append(
            String.format(
                "%s%n%s%n%s%n%s%n%s%n%s%n", " ", "",
                "请求Headers>>> " + request.headers().toString(),
                "请求URL>>> $httpUrl",
                "API>>> $api",
                "请求方法>>> " + request.method(),
            )
        )
        if (request.method() == "POST" || request.method() == "PUT") {
            builder.append(
                String.format("%s%n",
                    run {
                        var msg = bodyToString(request.body())
                        if (msg != null) {
                            msg = msg.replace("%(?![0-9a-fA-F]{2})".toRegex(), "%25")
                            msg = msg.replace("\\+".toRegex(), "%2B");
                        }
                        "请求参数>>> $msg"
                    })
            )
        }

        val response = chain.proceed(request)
        val t2 = System.nanoTime() //收到响应的时间
        val responseBody = response.peekBody(1024 * 1024.toLong())
        val result = responseBody.string()
        builder.append(
            String.format(
                "%s%n", "请求耗时>>> " + String.format("%.1f", (t2 - t1) / 1e6) + "ms"
            )
        )
        builder.append(String.format("%s%n%s%n%s%n", "请求结果>>> $result", " ", ""))
        LogUtil.i(LogUtil.TAG_NET, builder.toString())
        return response
    }

    fun bodyToString(request: RequestBody?): String? {
        return try {
            val buffer = Buffer()
            if (request != null) request.writeTo(buffer) else return ""
            buffer.readUtf8()
        } catch (e: IOException) {
            "did not work"
        }
    }
}