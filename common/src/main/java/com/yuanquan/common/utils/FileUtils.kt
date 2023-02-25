package com.yuanquan.common.utils

import android.text.TextUtils
import java.io.File

object FileUtils {
    @JvmStatic
    val File.extension: String get() = name.substringAfterLast('.', "")

    @JvmStatic
    fun getParseFormat(fileName: String): String {
        return fileName.substring(fileName.lastIndexOf(".") + 1)
    }

    @JvmStatic
    fun getParseName(url: String): String? {
        var fileName: String? = null
        try {
            fileName = url.substring(
                url.lastIndexOf("/") + 1,
                url.lastIndexOf(".")
            ) + System.currentTimeMillis()
        } finally {
            if (TextUtils.isEmpty(fileName)) {
                fileName = System.currentTimeMillis().toString()
            }
        }
        return fileName
    }
}