package com.yuanquan.common.utils

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.WindowManager
import com.yuanquan.common.R
import java.io.File


object SysUtils {
    @JvmStatic
    fun dp2Px(context: Context, dp: Float): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    @JvmStatic
    fun px2Dp(context: Context, px: Float): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (px / scale + 0.5f).toInt()
    }

    // 获取当前APP名称
    @JvmStatic
    fun getAppName(context: Context): String {
        val packageManager = context.packageManager
        val applicationInfo: ApplicationInfo = try {
            packageManager.getApplicationInfo(context.packageName, 0)
        } catch (e: java.lang.Exception) {
            return context.resources.getString(R.string.app_name)
        }
        return packageManager.getApplicationLabel(applicationInfo).toString()
    }

    @JvmStatic
    fun getAppVersion(context: Context): String {
        val manager: PackageManager = context.packageManager
        return try {
            val info: PackageInfo = manager.getPackageInfo(context.packageName, 0)
            info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "1.0.0"
        }
    }

    @JvmStatic
    fun getAppVersionCode(context: Context): Int {
        val manager: PackageManager = context.packageManager
        return try {
            val info: PackageInfo = manager.getPackageInfo(context.packageName, 0)
            info.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            1
        }
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    @JvmStatic
    fun getSystemModel(): String? {
        return try {
            Build.MODEL
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    @JvmStatic
    fun getDeviceBrand(): String? {
        return try {
            Build.BRAND
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 获取设备制造商
     *
     * @return 设备制造商
     */
    @JvmStatic
    fun getDeviceManufacturer(): String? {
        return try {
            Build.MANUFACTURER.toLowerCase()
        } catch (e: Exception) {
            ""
        }
    }

    @JvmStatic
    fun initFiles() {
        var file = File(Environment.getExternalStorageDirectory(), "data")
        if (!file.exists()) file.mkdirs()
        file = File(Environment.getExternalStorageDirectory(), "images")
        if (!file.exists()) file.mkdirs()
        file = File(Environment.getExternalStorageDirectory(), "download")
        if (!file.exists()) file.mkdirs()
    }

    @JvmStatic
    fun getScreenWidth(activity: Activity): Int {
        var width = 0
        val windowManager = activity.windowManager
        val display = windowManager.defaultDisplay
        width = display.width
        return width
    }

    @JvmStatic
    fun getScreenHeight(activity: Activity): Int {
        var height = 0
        val windowManager = activity.windowManager
        val display = windowManager.defaultDisplay
        height = display.height
        return height
    }

    /**
     * 根据屏幕方向判断是否是横屏
     */
    @JvmStatic
    fun isLandscapeScreen(context: Context): Boolean {
        val ori = context.resources.configuration.orientation
        return ori == Configuration.ORIENTATION_LANDSCAPE
    }

    /**
     * 根据屏幕宽高比判断是否是竖屏
     */
    @JvmStatic
    fun isPortraitScreen(context: Context): Boolean {
        return getPhoneWidthPixels(context) < getPhoneHeightPixels(context)
    }

    @JvmStatic
    fun getPhoneWidthPixels(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    @JvmStatic
    fun getPhoneHeightPixels(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    /**
     * 判断是否平板设备
     * @param context
     * @return true:平板,false:手机
     */
    @JvmStatic
    fun isTablet(context: Context): Boolean {
        val configuration = context.resources.configuration
        return configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    /**
     * 判断是否是全面屏
     */
    @Volatile
    private var mHasCheckAllScreen = false

    @Volatile
    private var mIsAllScreenDevice = false

    @JvmStatic
    fun isAllScreenDevice(context: Context): Boolean {
        if (mHasCheckAllScreen) {
            return mIsAllScreenDevice
        }
        mHasCheckAllScreen = true
        mIsAllScreenDevice = false
        // 低于 API 21的，都不会是全面屏。。。
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false
        }
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (windowManager != null) {
            val display = windowManager.defaultDisplay
            val point = Point()
            display.getRealSize(point)
            val width: Float
            val height: Float
            if (point.x < point.y) {
                width = point.x.toFloat()
                height = point.y.toFloat()
            } else {
                width = point.y.toFloat()
                height = point.x.toFloat()
            }
            if (height / width >= 1.97f) {
                mIsAllScreenDevice = true
            }
        }
        return mIsAllScreenDevice
    }

    /**
     * 设置屏幕常亮
     *
     * @param activity
     * @param b
     */
    @JvmStatic
    fun theScreenIsAlwaysOn(activity: Activity, b: Boolean) {
        if (b) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @JvmStatic
    fun getScreenSize(context: Context): Point {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val out = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(out)
        } else {
            val width = display.width
            val height = display.height
            out[width] = height
        }
        return out
    }

    @JvmStatic
    fun isScreenRotationEnabled(context: Context): Boolean {
        val rotationSetting: Int = Settings.System.getInt(
            context.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION, 0
        )
        return rotationSetting == 1
    }
}