package com.yuanquan.common.ui.webview

import android.content.Context
import android.os.Build
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebSettings
import android.webkit.WebView
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

    @JvmStatic
    fun removeAllCookie(context: Context) {
        CookieSyncManager.createInstance(context)
        val cookieManager = CookieManager.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //这个代码是清楚webview里的所有cookie加不加完全看你自己。
            cookieManager?.removeSessionCookies(null)
            cookieManager?.flush()
        } else {
            cookieManager?.removeAllCookie()
            CookieSyncManager.getInstance().sync()
        }
    }

    @JvmStatic
    fun setWebViewSettings(context: Context, mWebView: WebView?) {
        mWebView?.requestFocusFromTouch()
        mWebView?.isDrawingCacheEnabled = true
        mWebView?.isVerticalScrollBarEnabled = false
        mWebView?.isHorizontalScrollBarEnabled = false
        mWebView?.setVerticalScrollbarOverlay(false)
        mWebView?.setHorizontalScrollbarOverlay(false)

        val webSetting = mWebView?.settings
        webSetting?.allowFileAccess = true
        webSetting?.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS //自适应屏幕
        webSetting?.setSupportZoom(false) // 设置可以支持缩放
        webSetting?.builtInZoomControls = false // 设置出现缩放工具
        webSetting?.useWideViewPort = false //扩大比例的缩放
        webSetting?.setSupportMultipleWindows(true)
//        webSetting.setLoadWithOverviewMode(true);
//        webSetting?.setAppCacheEnabled(true)
//        webSetting.setDatabaseEnabled(true);
        webSetting?.domStorageEnabled = true
        webSetting?.javaScriptEnabled = true //支持javascript

        webSetting?.setGeolocationEnabled(true)
//        webSetting?.setAppCacheMaxSize(Long.MAX_VALUE)
//        webSetting?.setAppCachePath(requireContext().getDir("appcache", 0).getPath())
//        webSetting?.databasePath = requireContext().getDir("databases", 0).getPath()


        webSetting?.defaultTextEncodingName = "UTF-8"
        webSetting?.setUserAgentString(webSetting.userAgentString)
        webSetting?.javaScriptCanOpenWindowsAutomatically = true
        webSetting?.loadsImagesAutomatically = true

        webSetting?.setGeolocationDatabasePath(
            context.getDir("geolocation", 0).getPath()
        )
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting?.pluginState = WebSettings.PluginState.ON_DEMAND
//        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // webSetting.setPreFectch(true);

        //        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // webSetting.setPreFectch(true);

        webSetting?.mediaPlaybackRequiresUserGesture = false
        webSetting?.setAllowFileAccess(true);
        webSetting?.setAllowFileAccessFromFileURLs(true);
        webSetting?.setAllowUniversalAccessFromFileURLs(true);
    }

    @JvmStatic
    fun loadDataWithBaseURL(url: String): String {
        return """<html> 
                <head> 
                <style type="text/css"> 
                body {font-size:18px;}
                </style> 
                </head> 
                <body><script type='text/javascript'>window.onload = function(){
                var ${"$"}img = document.getElementsByTagName('img');
                for(var p in  ${"$"}img){
                 ${"$"}img[p].style.width = '100%%';
                ${"$"}img[p].style.height ='auto'
                }
                }</script>$url</body></html>"""
    }
}