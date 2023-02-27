package com.yuanquan.common.ui.webview

import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat.startActivity

open class MyWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, overrideUrl: String): Boolean {
        try {
            if (overrideUrl.contains("tel://") || overrideUrl.contains("tel:")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(overrideUrl))
                view.context.startActivity(intent)
                return true
            }
        } catch (e: Exception) {

        }
        return super.shouldOverrideUrlLoading(view, overrideUrl)
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
//        super.onReceivedSslError(view, handler, error)
        handler?.proceed()
    }
}