package com.yuanquan.common.ui.common

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import com.yuanquan.common.databinding.FragmentWebViewBinding
import com.yuanquan.common.event.EventMessage
import com.yuanquan.common.App
import com.yuanquan.common.BuildConfig
import com.yuanquan.common.R
import com.yuanquan.common.event.EventCode
import com.yuanquan.common.ui.base.BaseFragment
import com.yuanquan.common.ui.base.BaseViewModel
import com.yuanquan.common.utils.CommonUtils
import com.yuanquan.common.utils.GlideManager
import com.yuanquan.common.utils.LogUtil
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
    var orientation: Int? = null

    private var mWebView: WebView? = null

    override fun initData() {
//        if (CommonUtils.getAppArea()) {
//            GlideManager.asGif(context, R.mipmap.twwebloading, vb.ivWebLoading)
//        } else {
//            GlideManager.asGif(context, R.mipmap.zhwebloading, vb.ivWebLoading)
//        }
        if (arguments != null) {
//            pageName = arguments?.getString("pageName")
            statusBarHeight = arguments?.getInt("statusBarHeight") ?: 0
            var cookies = arguments?.getStringArrayList("cookies")
            orientation =
                arguments?.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            if (orientation != null && orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                activity?.requestedOrientation = orientation!!
            }
            url = arguments?.getString("url") ?: ""
            WebViewUtils.syncCookie(mContext, url, cookies)
            initWebView()
            loadWebViewUrl(false)
            if (BuildConfig.DEBUG) {
                Log.e("WebViewUrl: ", url)
            }
        }

        mWebView?.setOnLongClickListener { true }


    }

    private fun theScreenIsAlwaysOn(boolean: Boolean) {
        SysUtils.theScreenIsAlwaysOn(mContext, boolean)
    }

    fun isScreenOriatationPortrait(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
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
        mWebView?.addJavascriptInterface(JavaScriptApp(), "app")
        mWebView?.requestFocusFromTouch()
        mWebView?.setDrawingCacheEnabled(true)
        mWebView?.setVerticalScrollBarEnabled(false)
        mWebView?.setHorizontalScrollBarEnabled(false)
        mWebView?.setVerticalScrollbarOverlay(false)
        mWebView?.setHorizontalScrollbarOverlay(false)

        val webSetting = mWebView?.getSettings()
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
            requireContext().getDir("geolocation", 0).getPath()
        )
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting?.pluginState = WebSettings.PluginState.ON_DEMAND
//        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // webSetting.setPreFectch(true);

        //        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // webSetting.setPreFectch(true);

//        webSettings = webView?.settings

        webSetting?.mediaPlaybackRequiresUserGesture = false
        webSetting?.userAgentString = webSetting?.userAgentString + " oookliveapp/0.1"
        //临时用于直播页面，后期可删除
        LogUtil.e("用户" + webSetting?.userAgentString)
        mWebView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, overrideUrl: String): Boolean {
                try {
                    if (overrideUrl.contains("tel://") || overrideUrl.contains("tel:")) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(overrideUrl))
                        startActivity(intent)
                        return true
                    }
//                    else if (!url.equals(CommonUtils.isOOOKURL(url))) {
//                        startActivity(
//                            Intent(context, WebActivity::class.java).putExtra(
//                                "url", overrideUrl
//                            )
//                        )
//                        return true
//                    }
                } catch (e: Exception) {

                }
                return super.shouldOverrideUrlLoading(view, overrideUrl)
            }

            override fun onReceivedSslError(
                view: WebView, handler: SslErrorHandler, error: SslError
            ) {
                handler.proceed()
            }

            override fun onLoadResource(p0: WebView?, p1: String?) {
                super.onLoadResource(p0, p1)
            }

            override fun onReceivedError(
                view: WebView, errorCode: Int, description: String, failingUrl: String
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                showErrorPage()
            }

            override fun onPageStarted(p0: WebView, p1: String?, p2: Bitmap?) {
                super.onPageStarted(p0, p1, p2)
                Log.e("TAG", "onPageStarted: " + p0.url)
            }

            override fun onPageFinished(p0: WebView?, p1: String?) {
                super.onPageFinished(p0, p1)
                //仅限app0.1版本 解决直播间两次执行的问题，后期可删除
                mWebView?.loadUrl("javascript: window.localStorage.getItem(\"ok\") != 1 && window.location.reload(true); window.localStorage.setItem(\"ok\",1);")
                isCreate = false
                var loadsImagesAutomatically = mWebView?.settings?.loadsImagesAutomatically
                if (loadsImagesAutomatically != null && !loadsImagesAutomatically) {
                    mWebView?.settings?.loadsImagesAutomatically = true
                }
                //                var blockNetworkImage = webSettings?.blockNetworkImage
                //                if (blockNetworkImage != null && blockNetworkImage) {
                //                    webSettings?.blockNetworkImage = false
                //                }
            }

            override fun onPageCommitVisible(p0: WebView?, p1: String?) {
                super.onPageCommitVisible(p0, p1)
                //                webDismissLoading()
                vb.ivWebLoading.visibility = View.GONE
            }
        }

        mWebView?.webChromeClient = object : WebChromeClient() {

            override fun onReceivedTitle(view: WebView, title: String) {
                if (!TextUtils.isEmpty(title)) {
                }
            }

//            override fun onGeolocationPermissionsShowPrompt(
//                origin: String,
//                callback: GeolocationPermissions.Callback
//            ) {
//
//                // Always grant permission since the app itself requires location
//                // permission and the user has therefore already granted it
//                super.onGeolocationPermissionsShowPrompt(origin, callback)
//                callback.invoke(origin, true, false)
//            }

            override fun onPermissionRequest(request: PermissionRequest) {
                Log.e("VideoChat", "onPermissionRequest")
                request.grant(request.resources)
                request.origin
            }

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

    fun loadWebViewUrl(reload: Boolean) {
        url = arguments?.getString("url") ?: ""
        if (url.isNullOrBlank() || url.isNullOrEmpty() || mWebView == null) {
            Log.e("TAG", "webView == null 或 url == null")
//            webShowLoading()
            return
        }
        if ("loadLocal" == mType) {
            url = """<html> 
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

            mWebView?.loadDataWithBaseURL(null, url, "text/html", "utf-8", null)
        } else {
            mWebView?.loadUrl(url)
//            webShowLoading()
        }
        if (reload) { //二次加载不刷新页面问题
            mWebView?.loadUrl("javascript:window.location.reload( true )");
        }
        vb.ivWebLoading.visibility = View.VISIBLE
    }

//    private fun saveFile(map: Map<String, String>?) {
//        var type = map?.get("type")
//        var data = map?.get("data")
//        if (StringUtil.isEmpty(data)) {
//            Toast.makeText(
//                context,
//                requireContext().getString(R.string.save_failed),
//                Toast.LENGTH_SHORT
//            )
//                .show()
//            return
//        }
//        if ("base64".equals(type)) {
//            var base64ToBitmap = MediaUtils.base64ToBitmap(data!!)
//            MediaUtils.saveToPicDir(requireContext(), base64ToBitmap)
//        } else if ("image".equals(type)) {
//            GlideManager.downloadBitmap(
//                context,
//                data,
//                object : GlideManager.OnDownloadBitmapListener {
//                    override fun onDownloadSuccess(bitmap: Bitmap) {
//                        MediaUtils.saveToPicDir(requireContext(), bitmap)
//                    }
//                })
//        } else {
//            showToast(context?.getString(R.string.save_failed))
//        }
//    }

    //    <--------------------------拍照功能-------------------------------------->
//    var bb = false  //一个标识位 防止dialog 消失的时候把 uploadMessageAboveL给置为null
//
//    /**
//     * 选择头像
//     */
//    private fun toSelectMultimedia() {
//        bb = false
//        if (MyApplication.getInstance().isLogined) {
//            PictureSelector.create(this)
//                .openGallery(PictureMimeType.ofImage())
//                .imageEngine(GlideEngine.createGlideEngine())
//                .maxSelectNum(1)// 最大图片选择数量
//                .minSelectNum(1)// 最小选择数量
//                .isCompress(true)
//                .forResult(object : OnResultCallbackListener<LocalMedia> {
//                    override fun onResult(result: MutableList<LocalMedia>) {
//                        bb = true
//                        var file = File(result[0].compressPath)
//                        var tempUri = Uri.fromFile(file)
//                        uploadMessageAboveL?.onReceiveValue(arrayOf(tempUri!!))
//                        uploadMessageAboveL = null
//                        uploadMessage?.onReceiveValue(tempUri!!)
//                        uploadMessage = null
//                    }
//
//                    override fun onCancel() {
//                        if (!bb) {
//                            uploadMessageAboveL?.onReceiveValue(null)
//                            uploadMessageAboveL = null
//                            uploadMessage?.onReceiveValue(null)
//                            uploadMessage = null
//                        }
//                    }
//
//                })
//        }
//    }

    //    <----------支付-------------->
//    var mBraintreeFragment: BraintreeFragment? = null
//
//    override fun tostartPayPal(data: PayPalNeedInfo, token: String) {
//        paypal(data, token)
//        setupBraintreeAndStartExpressCheckout(data)
//    }
//
//    fun setupBraintreeAndStartExpressCheckout(data: PayPalNeedInfo) {
//        val request =
//            PayPalRequest(data.moneyOrder).currencyCode("USD").intent(PayPalRequest.INTENT_SALE)
//        if (mBraintreeFragment != null) PayPal.requestOneTimePayment(
//            mBraintreeFragment,
//            request
//        )
//    }


//    fun paypal(orderNeedInfo: PayPalNeedInfo, token: String) {
//        try {
//            mBraintreeFragment = BraintreeFragment.newInstance(
//                this, token
//            )
//            mBraintreeFragment!!.addListener(object : ConfigurationListener {
//                override fun onConfigurationFetched(configuration: Configuration) {
//                }
//            })
//            mBraintreeFragment!!.addListener(object : PaymentMethodNonceCreatedListener {
//                override fun onPaymentMethodNonceCreated(paymentMethodNonce: PaymentMethodNonce) {
//                    val nonce = paymentMethodNonce.getNonce()
//                    postNonceToServer(nonce, orderNeedInfo)
//                }
//
//            })
//            // 取消监听
//            mBraintreeFragment!!.addListener(object : BraintreeCancelListener {
//                override fun onCancel(requestCode: Int) {
////                    webDismissLoading()
//                    iv_web_loading.visibility = View.GONE
//                }
//            })
//            // 错误监听
//            mBraintreeFragment!!.addListener(object : BraintreeErrorListener {
//                override fun onError(error: java.lang.Exception) {
////                    webDismissLoading()
//                    iv_web_loading.visibility = View.GONE
//                    if (error is ErrorWithResponse) {
//                        val errorWithResponse = error as ErrorWithResponse
//                        val cardErrors = errorWithResponse.errorFor("creditCard")
//                        if (cardErrors != null) {
//                            // There is an issue with the credit card.
//                            val expirationMonthError = cardErrors.errorFor("expirationMonth")
//                            if (expirationMonthError != null) {
//                                // There is an issue with the expiration month.
//                            }
//                        }
//                    }
//                }
//
//            })
//            // mBraintreeFragment is ready to use!
//        } catch (e: InvalidArgumentException) {
//            // There was an issue with your authorization string.
////            webDismissLoading()
//            iv_web_loading.visibility = View.GONE
//        }
//    }

    /**
     * 拿到nonce后  将值传给后台
     */
//    private fun postNonceToServer(nonce: String, orderNeed: PayPalNeedInfo) {
//        val dataMap = hashMapOf(
//            "nonce" to nonce,
//            "orderId" to orderNeed.id,
//            "plantForm" to "2",
//            "paymentCode" to "15"
//        )
//        mPresenter?.toRequestPayPal(dataMap)
//    }

    /**
     * paypal支付成功后
     */
//    override fun payPalPaded(data: ExcuteBrainTreePayInfo) {
//        toStartPaymentResult(data.orderId, 2)
//    }

    /**
     * 跳转到支付成功界面
     * mpayType  0 支付宝  1微信  2paypal
     */
//    private fun toStartPaymentResult(morderId: String, mpayType: Int) {
//        val intent =
//            Intent(context, PayResultActivity::class.java).putExtra("orderId", morderId)
//                .putExtra("payType", mpayType)
//        startActivity(intent)
//        activity?.setResult(1008)
//    }


    var orderId = ""  //本地的订单id

//    /**
//     * 调起微信支付
//     */
//    private fun toWPay(json: String?) {
//        if (json.isNullOrEmpty()) {
//            return
//        }
//        val parseObject = JSONObject.parseObject(json, WeChatPayInfo::class.java)
//        if (parseObject.id != null) {
//            orderId = parseObject.id
//        } else {
//            showToast(getString(R.string.order_message_error))
//            return
//        }
//        val api = WXAPIFactory.createWXAPI(context, null)
//        api.registerApp(parseObject.appid)
//        val payReq = PayReq()
//        payReq.appId = parseObject.appid
//        payReq.partnerId = parseObject.partnerid
//        payReq.prepayId = parseObject.prepayid
//        payReq.nonceStr = parseObject.noncestr
//        payReq.timeStamp = parseObject.timestamp
//        payReq.packageValue = "Sign=WXPay"
//        payReq.sign = parseObject.sign
//        api.sendReq(payReq)
//    }


//    /**
//     * 支付宝支付
//     */
//    private fun toAliPay(json: String) {
//        if (json.isNullOrBlank()) {
//            return
//        }
//        val parseObject = JSONObject.parseObject(json, AliBean::class.java)
//        if (parseObject.orders.id != null) {
//            orderId = parseObject.orders.id
//        } else {
//            showToast(getString(R.string.order_message_error))
//            return
//        }
//        if (parseObject.responseBody.isNullOrEmpty()) {
//            return
//        }
//        val runable = Runnable {
//            run {
//                val payTask = PayTask(activity)
//                val payV2 = payTask.payV2(parseObject.responseBody, true)
//                val msg = handler.obtainMessage()
//                msg.obj = payV2
//                msg.what = 9
//                handler.sendMessage(msg)
//            }
//        }
//        val payThread = Thread(runable)
//        payThread.start()
//
//    }

//    <----------支付-------------->

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