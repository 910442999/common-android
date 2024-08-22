package com.yuanquan.common.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.fragment.app.FragmentActivity

/**
 * @author：luck
 * @date：2021/11/17 4:42 下午
 * @describe：ActivityCompatHelper
 */
object ActivityCompatHelper {
    private const val MIN_FRAGMENT_COUNT = 1

    @JvmStatic
    fun isDestroy(activity: Activity?): Boolean {
        return if (activity == null) {
            true
        } else activity.isFinishing || activity.isDestroyed
    }

    /**
     * 验证Fragment是否已存在
     *
     * @param fragmentTag Fragment标签
     * @return
     */
    @JvmStatic
    fun checkFragmentNonExits(activity: FragmentActivity, fragmentTag: String?): Boolean {
        if (isDestroy(activity)) {
            return false
        }
        val fragment = activity.supportFragmentManager.findFragmentByTag(fragmentTag)
        return fragment == null
    }

    @JvmStatic
    fun assertValidRequest(context: Context?): Boolean {
        if (context is Activity) {
            return !isDestroy(context)
        } else if (context is ContextWrapper) {
            val contextWrapper = context
            if (contextWrapper.baseContext is Activity) {
                val activity = contextWrapper.baseContext as Activity
                return !isDestroy(activity)
            }
        }
        return true
    }

    /**
     * 验证当前是否是根Fragment
     *
     * @param activity
     * @return
     */
    @JvmStatic
    fun checkRootFragment(activity: FragmentActivity): Boolean {
        return if (isDestroy(activity)) {
            false
        } else activity.supportFragmentManager.backStackEntryCount == MIN_FRAGMENT_COUNT
    }

    @JvmStatic
    fun isActivityInStack(context: Context, activityClass: Class<*>): Boolean {
        val activityManager =
            context.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
        val taskInfoList = activityManager.getRunningTasks(Int.MAX_VALUE)
        for (taskInfo in taskInfoList) {
            val componentName = taskInfo.baseActivity
            var b = activityClass.name == componentName?.className
            if (componentName != null && b) {
                return true
            }
        }
        return false
    }

    /**
     * 通过反射启动页面
     *
     *     ActivityCompatHelper.startActivity(
     *                 context, "com.yuanquan.common.ui.TestActivity",
     *                 intent
     *             )
     *
     */
    fun startActivityForClass(context: Context, className: String, intent: Intent) {
        try {
            val clazz = Class.forName(className)
            // 启动Activity
            intent.setClass(context, clazz)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     *     ActivityCompatHelper.startActivityForClassName(
     *                 context, "com.yuanquan.common.ui.TestActivity",
     *                 intent
     *             )
     */
    fun startActivityForClassName(context: Context, className: String, intent: Intent) {
        try {
            // 启动Activity
            intent.setClassName(context, className)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}