package com.yuanquan.common.ui.common

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.yuanquan.common.R
import com.yuanquan.common.databinding.ActivityWebViewBinding
import com.yuanquan.common.ui.base.BaseActivity
import com.yuanquan.common.ui.base.BaseViewModel

class WebViewActivity :
    BaseActivity<BaseViewModel<ActivityWebViewBinding>, ActivityWebViewBinding>() {
    override fun onPageName(): String? {
        return title
    }

    var title: String? = null
    var webViewFragment: WebViewFragment? = null
    var orientation: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        var fullScreen = intent.getBooleanExtra("fullScreen", false)
        if (fullScreen) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        super.onCreate(savedInstanceState)
    }

    override fun initData() {
        var pageName = intent.getStringExtra("pageName")
        title = intent.getStringExtra("title") ?: ""
        var url = intent.getStringExtra("url") ?: ""
        var showBar = intent.getBooleanExtra("showBar", true)
        var showTitle = intent.getBooleanExtra("showTitle", true)
        var shareImage = intent.getStringExtra("shareImage") ?: ""
        var shareContent = intent.getStringExtra("shareContent") ?: ""
        var cookies = intent.getStringArrayListExtra("cookies")
        var orientation =
            intent.getIntExtra("orientation", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        if (showBar) {
            vb.layoutToolbar.vTitleBarLine.visibility = View.VISIBLE
        } else {
            vb.layoutToolbar.vTitleBarLine.visibility = View.GONE
        }
        if (showTitle) {
            vb.layoutToolbar.rlTitleToolbar.visibility = View.VISIBLE
            if (!title.isNullOrBlank()) {
                vb.layoutToolbar.titleToolbar.text = title
            }
            if (!shareImage.isNullOrBlank() || !shareContent.isNullOrBlank()) vb.layoutToolbar.ivTitleRight.visibility =
                View.VISIBLE
        } else {
            vb.layoutToolbar.rlTitleToolbar.visibility = View.GONE
        }
        var bundle = Bundle()
        bundle.putString("type", intent.getStringExtra("type"))
        bundle.putString("url", url)
        bundle.putString("title", title)
        bundle.putString("pageName", pageName)
        bundle.putInt("orientation", orientation)
        bundle.putStringArrayList("cookies", cookies)
        webViewFragment = WebViewFragment()
        webViewFragment?.arguments = bundle
        this.supportFragmentManager.beginTransaction().add(R.id.fl_content, webViewFragment!!)
            .show(webViewFragment!!).commitAllowingStateLoss()

//        iv_title_right.click {
//            var shareDialog = ShareDialog(context)
//            shareDialog.builder().setCancelable(true)
//                .setCanceledOnTouchOutside(true)
//                .setOnItemClickListener(object : ShareDialog.OnItemClickListener {
//                    override fun onClick(which: String) {
//                        shareDialog.toShowShare(
//                            which,
//                            title ?: "",
//                            shareContent ?: "",
//                            url,
//                            shareImage
//                        )
//                    }
//
//                    override fun onCopyUrl() {
//                        shareDialog.onCopyUrl(url)
//                    }
//                }).show()
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (webViewFragment != null) {
            webViewFragment?.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() === KeyEvent.ACTION_DOWN) {
            if (webViewFragment != null && webViewFragment?.canGoBack()!!) {
                webViewFragment?.goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        webViewFragment?.onClearDestroy()
        webViewFragment = null
        super.onDestroy()
    }

    override fun initView() {

    }
}
