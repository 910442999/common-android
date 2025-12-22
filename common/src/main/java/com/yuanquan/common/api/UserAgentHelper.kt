package com.yuanquan.common.api

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import okhttp3.OkHttp
import okhttp3.OkHttpClient

object UserAgentHelper {
    /**
     * 生成默认的用户代理字符串
     * 格式：应用名/版本号 (包名; build:构建版本; Android 系统版本) OkHttp/版本号
     */
    fun generateDefaultUserAgent(context: Context): String {
        // 获取应用信息
        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        } catch (e: Exception) {
            null
        }

        val appName = getApplicationName(context)
        val packageName = context.packageName
        val versionName = packageInfo?.versionName ?: "Unknown"
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo?.longVersionCode?.toString() ?: "Unknown"
        } else {
            @Suppress("DEPRECATION")
            packageInfo?.versionCode?.toString() ?: "Unknown"
        }

        // 获取系统信息
        val androidVersion = "Android ${Build.VERSION.RELEASE}"
        val sdkVersion = Build.VERSION.SDK_INT

        // 获取设备信息（可选）
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        var httpVersion = "OkHttp/${OkHttp.VERSION}"
        // 构建User-Agent字符串
        return "$appName/$versionName ($packageName; build:$versionCode; $androidVersion SDK $sdkVersion; $manufacturer $model) $httpVersion"
    }

    /**
     * 获取应用名称
     */
    private fun getApplicationName(context: Context): String {
        val applicationInfo = context.applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0) {
            applicationInfo.nonLocalizedLabel?.toString() ?: "Unknown"
        } else {
            context.getString(stringId)
        }
    }
}