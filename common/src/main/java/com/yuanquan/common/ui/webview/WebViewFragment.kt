package com.yuanquan.common.ui.webview

import android.net.Uri
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.FrameLayout
import com.yuanquan.common.BuildConfig
import com.yuanquan.common.databinding.FragmentWebViewBinding
import com.yuanquan.common.ui.base.BaseFragment
import com.yuanquan.common.ui.base.BaseViewModel
import com.yuanquan.common.utils.SysUtils


class WebViewFragment :
    BaseFragment<BaseViewModel<FragmentWebViewBinding>, FragmentWebViewBinding>() {

    var url = ""
    var mType: String = "" //loadLocal 加载本地
    var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null
    var uploadMessage: ValueCallback<Uri>? = null
    var acceptTypes: Array<String>? = null

    var theScreenIsAlwaysOn = false // 屏幕始终常亮

    var isCreate = true
    var statusBarHeight: Int = 0

    private var mWebView: WebView? = null

    override fun initData() {
        if (arguments != null) {
            statusBarHeight = arguments?.getInt("statusBarHeight") ?: 0
            var cookies = arguments?.getStringArrayList("cookies")
            url = arguments?.getString("url") ?: ""
            WebViewUtils.syncCookie(mContext, url, cookies)
            initWebView()
            loadWebViewUrl(false)
            if (BuildConfig.DEBUG) {
                Log.e("WebViewUrl: ", url)
            }
        }

        mWebView?.setOnLongClickListener { true }
        WebViewUtils.setWebViewSettings(requireContext(), mWebView)
        mWebView?.addJavascriptInterface(JavaScriptApp(), "app")
    }

    private fun theScreenIsAlwaysOn(boolean: Boolean) {
        SysUtils.theScreenIsAlwaysOn(mContext, boolean)
    }

    private fun initWebView() {
        if (BuildConfig.DEBUG) { //如果是测试环境 就打开调试
            WebView.setWebContentsDebuggingEnabled(true)
        }
//        if ("save" == mType && Build.VERSION.SDK_INT >= 21) {
//            //为了减少内存占用以提高性能，因此在默认情况下会智能的绘制html中需要绘制的部分，其实就是当前屏幕展示的html内容，因此会出现未显示的图像是空白的
////            android.webkit.WebView.enableSlowWholeDocumentDraw()
//        }
        //        WebStorage.getInstance().deleteAllData()
        //        if (webView?.getParent() != null) {
        //            (webView?.getParent() as ViewGroup).removeView(webView)
        //        }
        //        webView = WebHelper.getWebView()
        //        webView?.clearHistory()
        //        webView?.requestFocus()

        mWebView = WebView(requireContext())
        vb.flWebView.addView(
            mWebView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        mWebView?.webViewClient = object : MyWebViewClient() {

            override fun onPageFinished(p0: WebView?, p1: String?) {
                super.onPageFinished(p0, p1)
                isCreate = false
                var loadsImagesAutomatically = p0?.settings?.loadsImagesAutomatically
                if (loadsImagesAutomatically != null && !loadsImagesAutomatically) {
                    p0?.settings?.loadsImagesAutomatically = true
                }
            }

            override fun onPageCommitVisible(p0: WebView?, p1: String?) {
                super.onPageCommitVisible(p0, p1)
                //                webDismissLoading()
                vb.ivWebLoading.visibility = View.GONE
            }
        }

        mWebView?.webChromeClient = object : MyWebChromeClient() {

            override fun onProgressChanged(p0: WebView?, newProgress: Int) {
                super.onProgressChanged(p0, newProgress)
                if (newProgress == 100) {
                    //                    webDismissLoading()
                }
            }

            override fun onHideCustomView() {
                super.onHideCustomView()
                //                if (callback != null) {
                //                    callback.onCustomViewHidden()
                //                    callback = null
                //                }
                //                if (myVideoView != null) {
                //                    val viewGroup = myVideoView.getParent()
                //                    viewGroup.removeView(myVideoView)
                //                    viewGroup.addView(myNormalView)
                //                }
            }

            /**
             * 全屏播放配置
             */
            //            override fun onShowCustomView(p0: View?, p1: IX5WebChromeClient.CustomViewCallback?) {
            //                super.onShowCustomView(p0, p1)
            //                View normalView = (View) findViewById(R.id.web_filechooser);
            //                ViewGroup viewGroup = (ViewGroup) normalView.getParent();
            //                viewGroup.removeView(normalView);
            //                viewGroup.addView(view);
            //                myVideoView = view;
            //                myNormalView = normalView;
            //                callback = customViewCallback;
            //            }

            //            override fun onJsAlert(
            //                p0: WebView?,
            //                p1: String?,
            //                p2: String?,
            //                p3: com.tencent.smtt.export.external.interfaces.JsResult?
            //            ): Boolean {
            //                /**
            //                 * 这里写入你自定义的window alert
            //                 */
            //                return super.onJsAlert(p0, p1, p2, p3)
            //
            //            }

            //        }

            //            override fun openFileChooser(
            //                valueCallback: ValueCallback<Uri>?, acceptType: String, capture: String?
            //            ) {
            //                super.openFileChooser(valueCallback, acceptType, capture)
            //                uploadMessage = valueCallback
            //                if (acceptType.contains("video")) {
            //                    requestPermissions(
            //                        1,
            //                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
            //                        Manifest.permission.CAMERA
            //                    )
            //                } else {
            //                    requestPermissions(
            //                        2,
            //                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
            //                        Manifest.permission.CAMERA
            //                    )
            //                }
            //            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                uploadMessageAboveL = filePathCallback
                var video = false
                acceptTypes = fileChooserParams.acceptTypes
                if (acceptTypes != null) {
                    acceptTypes?.forEach {
                        if (it.contains("video")) video = true
                    }
                    //                    if (video) {
                    //                        requestPermissions(
                    //                            1,
                    //                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    //                            Manifest.permission.CAMERA
                    //                        )
                    //                    } else {
                    //                        requestPermissions(
                    //                            2,
                    //                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    //                            Manifest.permission.READ_EXTERNAL_STORAGE
                    //                        )
                    //
                    //                    }
                }

                return true
            }

        }

        mWebView?.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && mWebView?.canGoBack()!!) {
                        mWebView?.goBack()   //后退
                        return true    //已处理
                    }
                }
                return false
            }
        })
        mWebView?.setDownloadListener(object : DownloadListener {
            override fun onDownloadStart(
                p0: String?, p1: String?, p2: String?, p3: String?, p4: Long
            ) {
                //				new AlertDialog.Builder(BrowserActivity.this)
                //						.setTitle("allow to download？")
                //						.setPositiveButton("yes",
                //								new DialogInterface.OnClickListener() {
                //									@Override
                //									public void onClick(DialogInterface dialog,
                //											int which) {
                //										Toast.makeText(
                //												BrowserActivity.this,
                //												"fake message: i'll download...",
                //												Toast.LENGTH_SHORT).show();
                //									}
                //								})
                //						.setNegativeButton("no",
                //								new DialogInterface.OnClickListener() {
                //
                //									@Override
                //									public void onClick(DialogInterface dialog,
                //											int which) {
                //										// TODO Auto-generated method stub
                //										Toast.makeText(
                //												BrowserActivity.this,
                //												"fake message: refuse download...",
                //												Toast.LENGTH_SHORT).show();
                //									}
                //								})
                //						.setOnCancelListener(
                //								new DialogInterface.OnCancelListener() {
                //
                //									@Override
                //									public void onCancel(DialogInterface dialog) {
                //										// TODO Auto-generated method stub
                //										Toast.makeText(
                //												BrowserActivity.this,
                //												"fake message: refuse download...",
                //												Toast.LENGTH_SHORT).show();
                //									}
                //								}).show();

            }

        });
    }


    fun showErrorPage() {
//        ll_error_page.visibility = View.VISIBLE
//        tv_again.setOnClickListener {
//            ll_error_page.setVisibility(View.GONE)
//            mWebView?.webChromeClient = null
////            webView?.webViewClient = null
//            mWebView?.settings?.setJavaScriptEnabled(false)
//            clearWebviewCache(false)
//            mWebView?.reload()
//        }
    }

    fun clearWebviewCache(loadEmpty: Boolean) {
        mWebView?.clearHistory();
        mWebView?.clearCache(true) //清除缓存
        if (loadEmpty) {
//            webView?.loadUrl("about:blank");
//            webView?.loadUrl("");
//            webView?.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
//            webView?.loadUrl("javascript:window.location.reload( true )");
        }
    }

    private fun loadWebViewUrl(reload: Boolean) {
        url = arguments?.getString("url") ?: ""
        if (url.isNullOrBlank() || url.isNullOrEmpty() || mWebView == null) {
            Log.e("TAG", "webView == null 或 url == null")
//            webShowLoading()
            return
        }
        if ("loadLocal" == mType) {
            mWebView?.loadDataWithBaseURL(
                null,
                WebViewUtils.loadDataWithBaseURL(url),
                "text/html",
                "utf-8",
                null
            )
        } else {
            mWebView?.loadUrl(url)
//            webShowLoading()
        }
        if (reload) { //二次加载不刷新页面问题
            mWebView?.loadUrl("javascript:window.location.reload( true )");
        }
        vb.ivWebLoading.visibility = View.VISIBLE
    }

    inner class JavaScriptApp {
        @JavascriptInterface
        fun toFinish() {
            activity?.finish()
        }
    }

    override fun onDestroy() {
        if (theScreenIsAlwaysOn) {
            theScreenIsAlwaysOn(false)
        }
        super.onDestroy()
    }

    fun onClearDestroy() {
        if (mWebView == null) {
            Log.e("tag", "aaaaaa")
        }
//        webView?.loadUrl("about:blank");
        mWebView?.stopLoading()
        // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
        mWebView?.settings?.javaScriptEnabled = false
        mWebView?.clearHistory()
        //        webView.clearView()
        //        webView.clearCache(true)//清除缓存
        mWebView?.removeJavascriptInterface("app")
        mWebView?.removeAllViews()
        mWebView?.destroy()
//        webDismissLoading()
        mWebView == null

        super.onDestroy()
    }


    fun canGoBack(): Boolean? {
        return mWebView?.canGoBack()
    }

    fun goBack() {
        mWebView?.goBack()
    }

    override fun initView() {

    }
}