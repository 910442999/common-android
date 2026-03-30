package com.yuanquan.common.utils


import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Date 2019/5/30 7:32 PM
 *
 * @author tangxin
 */

object StatusBarUtil {
    /**
     * 修改状态栏颜色，支持4.4以上版本
     *
     * @param colorId 颜色
     */
    @JvmStatic
    fun setStatusBarColor(activity: Activity, colorId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.statusBarColor = colorId
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //使用SystemBarTintManager,需要先将状态栏设置为透明
            setTranslucentStatus(activity)
            val systemBarTintManager = SystemBarTintManager(activity)
            systemBarTintManager.isStatusBarTintEnabled = true//显示状态栏
            systemBarTintManager.setStatusBarTintColor(colorId)//设置状态栏颜色
        }
    }

    /**
     * 设置状态栏透明
     */
    @TargetApi(19)
    @JvmStatic
    fun setTranslucentStatus(activity: Activity) {
        setTranslucentStatus(activity, false, false, Color.TRANSPARENT, null)
    }

    @TargetApi(19)
    @JvmStatic
    fun setTranslucentStatus(
        activity: Activity,
        darkStatusBarText: Boolean,
        darkNavigationIcons: Boolean
    ) {
        setTranslucentStatus(
            activity,
            darkStatusBarText,
            darkNavigationIcons,
            Color.TRANSPARENT,
            null
        )
    }

    @TargetApi(19)
    @JvmStatic
    fun setTranslucentStatus(
        activity: Activity,
        darkStatusBarText: Boolean,
        darkNavigationIcons: Boolean,
        statusBarColor: Int,
        navigationBarColor: Int?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
            val window = activity.window
            val decorView = window.decorView
            //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
            val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            decorView.systemUiVisibility = decorView.systemUiVisibility or option
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = statusBarColor
            if (navigationBarColor != null) {
                window.navigationBarColor = navigationBarColor
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window = activity.window
            val attributes = window.attributes
            val flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            attributes.flags = attributes.flags or flagTranslucentStatus
            //int flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            //attributes.flags |= flagTranslucentNavigation;
            window.attributes = attributes
        }
        setSystemBarIconTheme(activity, darkStatusBarText, darkNavigationIcons)
    }

    @JvmStatic
    fun clearTranslucentStatus(activity: Activity) {
        val window = activity.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val decorView = window.decorView
            var flags = decorView.systemUiVisibility
            flags = flags and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.inv()
            decorView.systemUiVisibility = flags
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.WHITE
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }


    /**
     * 代码实现android:fitsSystemWindows
     *
     * @param activity
     */
    @JvmStatic
    fun setRootViewFitsSystemWindows(activity: Activity, fitSystemWindows: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val winContent = activity.findViewById<View>(android.R.id.content) as ViewGroup
            if (winContent.childCount > 0) {
                val rootView = winContent.getChildAt(0)
                rootView.fitsSystemWindows = fitSystemWindows
            }
        }
    }

    //获取状态栏高度
    @JvmStatic
    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier(
            "status_bar_height", "dimen", "android"
        )
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    /**
     * 设置状态栏文字颜色
     */
    @JvmStatic
    fun setStatusBarDarkTheme(activity: Activity, isDarkText: Boolean) {
        setSystemBarIconTheme(activity, isDarkText, isDarkText)
    }

    @JvmStatic
    fun setSystemBarIconTheme(
        activity: Activity,
        darkStatusBarText: Boolean,
        darkNavigationIcons: Boolean
    ) {
        val window = activity.window
        val decorView = window.decorView
        val controller = WindowInsetsControllerCompat(window, decorView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            controller.isAppearanceLightStatusBars = darkStatusBarText
            var flags = decorView.systemUiVisibility
            flags = if (darkStatusBarText) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            decorView.systemUiVisibility = flags
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            controller.isAppearanceLightNavigationBars = darkNavigationIcons
            var flags = decorView.systemUiVisibility
            flags = if (darkNavigationIcons) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
            decorView.systemUiVisibility = flags
        }
        //异形屏适配
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            window.isNavigationBarContrastEnforced = false
//        }
        //布局防遮挡处理
//        window.decorView.apply {
//            systemUiVisibility =
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//        }
    }
}
