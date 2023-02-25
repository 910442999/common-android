package com.yuanquan.common

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.yuanquan.common.utils.AppUpdateUtils
import com.yuanquan.common.utils.CrashHandler

open class App : Application() {
    var isAppBackstage = false

    companion object {
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        CrashHandler.getInstance().init(applicationContext)
        AppUpdateUtils.init(this)
        registerActivityLifecycleCallbacks()
    }

    private fun registerActivityLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var activityCount = 0
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                activityCount++
                if (activityCount == 1) { // 应用回到前台
                    isAppBackstage = false
                    Log.e(
                        "TAG",
                        "onActivityStarted: " + activity.javaClass.name + "   " + isAppBackstage
                    )
                }
            }

            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                activityCount--
                if (activityCount == 0) { // 应用回到后台
                    isAppBackstage = true
                    Log.e(
                        "TAG",
                        "onActivityStopped: " + activity.javaClass.name + "   " + isAppBackstage
                    )
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}