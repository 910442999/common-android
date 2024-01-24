package com.yuanquan.common

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import com.yuanquan.common.utils.CommonUtils
import com.yuanquan.common.utils.SPUtils
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.onAdaptListener
import me.jessyan.autosize.utils.ScreenUtils
class MyApp : Application() {
    companion object {
        var DEBUG: Boolean = true
        lateinit var instance: MyApp
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        AutoSizeConfig.getInstance().onAdaptListener = object : onAdaptListener {
            override fun onAdaptBefore(target: Any, activity: Activity) {
                AutoSizeConfig.getInstance().screenWidth = ScreenUtils.getScreenSize(activity)[0]
                AutoSizeConfig.getInstance().screenHeight = ScreenUtils.getScreenSize(activity)[1]
                //根据屏幕方向，设置适配基准
                if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    AutoSizeConfig.getInstance().designWidthInDp =
                        794                    //设置横屏基准
                    AutoSizeConfig.getInstance().designHeightInDp = 375
                } else {
                    AutoSizeConfig.getInstance().designHeightInDp = 794
                    AutoSizeConfig.getInstance().designWidthInDp =
                        375                    //设置竖屏基准
                }
            }

            override fun onAdaptAfter(target: Any?, activity: Activity?) {}
        }
    }
}