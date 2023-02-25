package com.yuanquan.common.ui.common

import android.content.Context
import android.os.Build
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import com.yuanquan.common.utils.LogUtil

object WebViewUtils {
    /**
     * 同步cookie
     *
     * @param url 地址
     * @param cookieList 需要添加的Cookie值,以键值对的方式:key=value
     */
    @JvmStatic
    fun syncCookie(context: Context, url: String, cookieList: ArrayList<String>?) {
        CookieSyncManager.createInstance(context)
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //这个代码是清楚webview里的所有cookie加不加完全看你自己。
//            cookieManager?.removeSessionCookies(null)
//            cookieManager?.flush()
//        } else {
//            cookieManager?.removeAllCookie()
//            CookieSyncManager.getInstance().sync()
//        }
        if (cookieList != null && cookieList.size > 0) {
            for (cookie in cookieList) {
                cookieManager.setCookie(url, cookie)
            }
        }
//        cookieManager.setCookie(url, "Domain=.zyb.com")
//        cookieManager.setCookie(url, "Path=/")
        val cookies = cookieManager.getCookie(url)
        LogUtil.e("webview cookies： " + cookies)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush()
        } else {
            CookieSyncManager.getInstance().sync()
        }
    }
}