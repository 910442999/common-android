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
        val startTime = System.currentTimeMillis() // 记录请求开始时间

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
        val responseBody = response.peekBody(1024 * 1024.toLong())
        val result = responseBody.string()
        val endTime = System.currentTimeMillis() // 记录请求结束时间

        val durationMillis = endTime - startTime // 计算请求时长，单位为毫秒
        // 将毫秒转换为分:秒:毫秒格式
        val seconds: Long = (durationMillis % 60000) / 1000
        val milliseconds: Long = durationMillis % 1000
        builder.append(
            String.format(
                "%s%n", String.format("请求耗时>>> %02d秒%03d毫秒", seconds, milliseconds)
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