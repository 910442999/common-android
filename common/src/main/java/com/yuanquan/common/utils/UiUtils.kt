package com.yuanquan.common.utils

import android.text.Html
import android.view.View
import android.view.ViewGroup.MarginLayoutParams

object UiUtils {
    /**
     * 设置Html.fromHtml(resource: String)
     */
    fun setHtmlText(text: String?): String {
        return Html.fromHtml(text).toString()
    }

    fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
        if (v.layoutParams is MarginLayoutParams) {
            val layoutParams = v.layoutParams as MarginLayoutParams
            layoutParams.setMargins(l, t, r, b)
        }
    }
    //根据文本获取颜色
    @JvmStatic
    fun getColourText(text: String?): String {
        var text = text
        if (text == null || text.isEmpty()) {
            text = ""
        }
        //获取位数
        val codePointAt = Character.codePointAt(text, 0)
        //转换16进制
        val hes = Integer.toHexString(codePointAt)
        var codePointAtLeng = hes.toString()
        // 如果转换完毕小于 4位数字默认加 1的 16位码
        if (codePointAtLeng.length < 4) {
            codePointAtLeng += this.gethes("1")
        }
        codePointAtLeng = codePointAtLeng.substring(0, 4)
        codePointAtLeng += "0a"
        return "#$codePointAtLeng"
    }

    @JvmStatic
    fun gethes(text: String): String {
        val codePointAt = Character.codePointAt(text, 0)
        val hes = Integer.toHexString(codePointAt)
        return hes
    }

}