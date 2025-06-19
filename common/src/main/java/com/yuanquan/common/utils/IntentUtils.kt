package com.yuanquan.common.utils

import android.content.Context
import android.content.Intent

object IntentUtils {
    @JvmStatic
    fun getIntentClassForName(context: Context, className: String): Intent {
        val clazz =
            Class.forName(className)
        val intent: Intent = Intent(context, clazz)
        return intent
    }
}