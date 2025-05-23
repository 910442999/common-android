package com.yuanquan.common.utils

import android.content.Context
import android.text.Html
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.yuanquan.common.widget.EditText_Clear

object UiUtils {
    /**
     * 设置Html.fromHtml(resource: String)
     */
    @JvmStatic
    fun setHtmlText(text: String?): String {
        return Html.fromHtml(text).toString()
    }

    @JvmStatic
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

    @JvmStatic
    fun setupEditTextNavigation(
        context: Context,
        currentEditText: EditText_Clear?,
        nextEditText: EditText_Clear?,
        confirmButton: TextView?,
        inputType: Int? = null
    ) {
        // 设置IME选项（需配合inputType）
        currentEditText?.imeOptions = if (nextEditText != null) {
            EditorInfo.IME_ACTION_NEXT
        } else inputType ?: EditorInfo.IME_ACTION_DONE
        // 保证IME选项生效的inputType配置
        currentEditText?.inputType =
            EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_NORMAL
        currentEditText?.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                when {
                    // 有下一个输入框时切换焦点
                    nextEditText != null -> {
                        nextEditText.requestFocus()
                        KeyBoardUtils.openKeyboard(nextEditText, context)
                    }
                    // 最后一个输入框触发确认操作
                    else -> {
                        confirmButton?.performClick()
                        KeyBoardUtils.closeKeyboard(currentEditText, context)
                    }
                }
                true
            } else {
                false
            }
        }
    }
}