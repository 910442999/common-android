package com.yuanquan.common.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object GooglePlayUtils {

    /**
     * 打开应用在 Google Play 的页面
     * @param context 上下文
     * @param packageName 应用包名，默认为当前应用
     */
    @JvmStatic
    fun openGooglePlay(context: Context, url: String?, packageName: String = context.packageName) {
        try {
            // 尝试使用 Google Play 应用打开
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                setPackage("com.android.vending")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // 处理没有安装 Google Play 的情况
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                LogUtil.e(e)
                // 最终回退方案：提示用户
//            Toast.makeText(
//                context,
//                "无法打开 Google Play 商店",
//                Toast.LENGTH_SHORT
//            ).show()
            }
        }
    }

    /**
     * 打开应用在 Google Play 的页面
     * @param context 上下文
     * @param packageName 应用包名，默认为当前应用
     */
    @JvmStatic
    fun openAppPage(context: Context, packageName: String = context.packageName) {
        try {
            // 尝试使用 Google Play 应用打开
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                setPackage("com.android.vending")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // 处理没有安装 Google Play 的情况
            handleNoGooglePlay(context, packageName)
        }
    }

    /**
     * 打开应用的评分页面
     * @param context 上下文
     * @param packageName 应用包名，默认为当前应用
     */
    @JvmStatic
    fun openAppReviewPage(context: Context, packageName: String = context.packageName) {
        try {
            // 直接跳转到评分页
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName&reviewId=0")
                setPackage("com.android.vending")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // 处理没有安装 Google Play 的情况
            handleNoGooglePlay(context, packageName)
        }
    }

    /**
     * 打开开发者页面
     * @param context 上下文
     * @param developerId 开发者ID（如：Google+ ID 或开发者名称）
     */
    @JvmStatic
    fun openDeveloperPage(context: Context, developerId: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://dev?id=$developerId")
                setPackage("com.android.vending")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/dev?id=$developerId")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * 在 Google Play 中搜索
     * @param context 上下文
     * @param query 搜索关键词
     */
    @JvmStatic
    fun searchOnGooglePlay(context: Context, query: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://search?q=$query")
                setPackage("com.android.vending")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/search?q=$query")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * 处理没有 Google Play 的情况
     */
    @JvmStatic
    private fun handleNoGooglePlay(context: Context, packageName: String) {
        // 尝试打开网页版 Google Play
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // 最终回退方案：提示用户
//            Toast.makeText(
//                context,
//                "无法打开 Google Play 商店",
//                Toast.LENGTH_SHORT
//            ).show()
        }
    }

    /**
     * 检查设备是否安装了 Google Play 商店
     */
    @JvmStatic
    fun isGooglePlayInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.android.vending", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取 Google Play 商店的版本号
     */
    @JvmStatic
    fun getGooglePlayVersion(context: Context): String? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo("com.android.vending", 0)
            packageInfo.versionName
        } catch (e: Exception) {
            null
        }
    }
}