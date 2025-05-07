package com.yuanquan.common.utils

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.yuanquan.common.model.DownloadInfo
import java.util.Arrays

/**
 * 可参考一下升级库
 * https://github.com/tangchao0106/AutoUpdateProject
 * implementation 'com.github.MZCretin:AutoUpdateProject:2.0.5'
 */
object AppUpdateUtils {
    @JvmStatic
    fun checkUpdate(
        context: Context,
        apkUrl: String?,
        prodVersionCode: Int,
        prodVersionName: String?,
        forceUpdateFlag: Int,
        updateLog: String?,
    ): DownloadInfo? {
        val info = DownloadInfo().setApkUrl(apkUrl).setProdVersionCode(prodVersionCode)
            .setProdVersionName(prodVersionName).setForceUpdateFlag(forceUpdateFlag)
            .setUpdateLog(updateLog)
        return this.checkUpdate(context, info)
    }

    @JvmStatic
    fun checkUpdate(context: Context, info: DownloadInfo?): DownloadInfo? {
        if (info == null) {
            return null
        }

        //检查当前版本是否需要更新 如果app当前的版本号大于等于线上最新的版本号 不需要升级版本
        val versionCode = SysUtils.getAppVersionCode(context)
        if (versionCode >= info.prodVersionCode) {
            return info
        }

        //如果用户开启了静默下载 其实是否开启强制更新已经没有意义了
        //检查是否强制更新
        if (info.forceUpdateFlag != 0) {
            //需要强制更新
            if (info.forceUpdateFlag == 1) {
                //hasAffectCodes拥有字段强制更新
                val hasAffectCodes = info.hasAffectCodes
                if (!TextUtils.isEmpty(hasAffectCodes)) {
                    val codes = Arrays.asList(
                        *hasAffectCodes.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray())
                    if (codes.contains(versionCode.toString() + "")) {
                        //包含这个版本 所以需要强制更新
                    } else {
                        //不包含这个版本 所以此版本需要强制更新
                        info.setForceUpdateFlag(0)
                    }
                }
            } else {
                //所有拥有字段强制更新
            }
        }
        return info
    }

    /**
     * 启动Activity
     *
     * @param context
     * @param info
     */
    @JvmStatic
    fun launchActivity(context: Context, info: DownloadInfo?, cla: Class<*>?) {
        val intent = Intent(context, cla)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra("info", info)
        context.startActivity(intent)
    }
}