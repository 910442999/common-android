package com.yuanquan.common.ui

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.yuanquan.common.databinding.ActivityTestLogDetailBinding
import com.yuanquan.common.ui.base.BaseActivity
import com.yuanquan.common.ui.base.BaseViewModel


class TestLogDetailActivity :
    BaseActivity<BaseViewModel<ActivityTestLogDetailBinding>, ActivityTestLogDetailBinding>() {
    var handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            vb.tvLog.text = msg.obj.toString()
//            vb.nestedScrollView.post { vb.nestedScrollView.fullScroll(View.FOCUS_DOWN) }
        }
    }

    override fun initView() {
    }

    override fun initData() {
        val data = intent.getStringExtra("data")
        if (data != null) {
            Thread { // 执行耗时操作
                // 耗时操作完成后, 发送消息到主线程的消息队列中
                // 使用 receivedFile 进行读取等操作
                val message = Message()
                message.obj = data.toString()
                handler.sendMessage(message)
            }.start()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}