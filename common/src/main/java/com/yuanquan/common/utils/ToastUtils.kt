package com.yuanquan.common.utils

import android.content.Context
import android.widget.Toast

object ToastUtils {
    private var oldMsg: String? = null
    private var oneTime: Long = 0
    private var twoTime: Long = 0
    private var toast: Toast? = null

    @JvmStatic
    fun show(context: Context, text: String?) {
        if (text == null || text == "null" || text == "") {
            return
        }
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
            toast?.show()
            oneTime = System.currentTimeMillis()
        } else {
            twoTime = System.currentTimeMillis()
            if (text == oldMsg) {
                if (twoTime - oneTime > 1000) {
                    // 这里是判断toast上一次显示的时间和这次的显示时间如果大于3000，
                    //  则显示新的toast
                    toast!!.cancel()
                    toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
                    toast?.show()
                    oneTime = twoTime
                }
            } else {
                toast!!.cancel()
                toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
                oldMsg = text
                toast?.show()
                oneTime = twoTime
            }
        }
    }

}