package com.yuanquan.common

import android.content.ClipData
import android.content.Context
import android.os.Bundle
import android.text.Selection.selectAll
import android.text.TextUtils
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import com.yuanquan.common.textview.SelectableTextView


class TextViewActivity : AppCompatActivity() {
    var tv_text1: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_view)
        tv_text1 = findViewById<TextView>(R.id.tv_text1)
        var tv_text2 = findViewById<TextView>(R.id.tv_text2)
        tv_text1?.text =
            "撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发"
        tv_text2.text =
            "撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发撒大发大发"

        tv_text1?.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            override fun onCreateActionMode(actionMode: ActionMode, menu: Menu?): Boolean {
                return true
            }

            override fun onPrepareActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
                menu?.let {
                    it.clear()
                    it.add(Menu.NONE, android.R.id.copy, 0, "复制")
//                    it.add(Menu.NONE, android.R.id.shareText, 2, "分享")
                }
                return true
            }

            override fun onActionItemClicked(actionMode: ActionMode?, menuItem: MenuItem): Boolean {
                val selStart: Int = tv_text1?.selectionStart ?: 0
                val selEnd: Int = tv_text1?.selectionEnd ?: 0
                val min = 0.coerceAtLeast(selStart.coerceAtMost(selEnd))
                val max = 0.coerceAtLeast(selStart.coerceAtLeast(selEnd))
                val content = tv_text1?.text?.subSequence(min, max)
                if (TextUtils.isEmpty(content)) {
                    return true
                }
                when (menuItem.itemId) {
                    android.R.id.copy -> {
                        Toast.makeText(
                            baseContext,
                            "复制  "+     content,
                            Toast.LENGTH_SHORT
                        ).show()
                        actionMode?.finish()
                    }

                    android.R.id.shareText -> {
                        try {
                            Toast.makeText(
                                baseContext,
                            "分享  "+    content,
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        } finally {
                            actionMode?.finish()
                        }
                    }
                }
                return true
            }

            override fun onDestroyActionMode(actionMode: ActionMode?) {}
        })
    }

    /**
     * 统一处理复制和剪切的操作
     * @param mode 用来区别是复制还是剪切
     * @return
     */
    private fun getSelectText(mode: SelectMode): String {
        //获取剪切班管理者
        //获取选中的起始位置
        val selectionStart: Int? = tv_text1?.getSelectionStart()
        val selectionEnd: Int? = tv_text1?.getSelectionEnd()
        Log.i("TAG", "selectionStart=$selectionStart,selectionEnd=$selectionEnd")
        //截取选中的文本
        var txt: String = tv_text1?.getText().toString()
        val substring = txt.substring(selectionStart ?: 0, selectionEnd ?: 0)
        Log.i("TAG", "substring=$substring")
        //将选中的文本放到剪切板
        //如果是复制就不往下操作了
        txt = txt.replace(substring, "")
        return txt
    }

    /**
     * 用枚举来区分是复制还是剪切
     */
    enum class SelectMode {
        COPY,
        CUT
    }

}