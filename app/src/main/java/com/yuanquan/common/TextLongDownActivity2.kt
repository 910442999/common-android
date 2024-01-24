package com.yuanquan.common

import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.text.Layout
import android.text.TextUtils
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.yuanquan.common.utils.LogUtil


class TextLongDownActivity2 : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_long_down2)
        var tv_text2 = findViewById<TextView>(R.id.tv_text2)
        tv_text2.setTextIsSelectable(true)
        tv_text2.customSelectionActionModeCallback = object : ActionMode.Callback2() {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                LogUtil.e("111111111111111111111111111111111111111")

                val selStart: Int = tv_text2?.selectionStart ?: 0
                val selEnd: Int = tv_text2?.selectionEnd ?: 0

                LogUtil.e("selStart " + selStart)
                LogUtil.e("selEnd " + selEnd)
                val min = 0.coerceAtLeast(selStart.coerceAtMost(selEnd))
                val max = 0.coerceAtLeast(selStart.coerceAtLeast(selEnd))

                LogUtil.e("min " + min)
                LogUtil.e("max " + max)
                val content = tv_text2?.text?.subSequence(min, max)

                LogUtil.e("content " + content)

                val layout: Layout = tv_text2.layout
                if (layout != null) {
                    val x: Float = layout.getPrimaryHorizontal(selStart)
                    val y: Int = layout.getLineBaseline(layout.getLineForOffset(selStart))

                    // 输出光标的坐标
                    LogUtil.e("坐标 x: $x, y: $y")
                }

                return false //返回false则不会显示弹窗
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menuInflater.inflate(R.menu.selection_action_menu, menu);
                if (menu != null && menu.size() > 0) {
                    for (i in 0 until menu.size()) {
                        val item = menu.getItem(i)
                        if ("剪贴板" == item.title.toString()) {
                            menu.removeItem(item.itemId)
                        }
                    }
                }
                LogUtil.e("2222222222222222222222222222222222222")
                return false
            }

            override fun onActionItemClicked(
                actionMode: ActionMode,
                menuItem: MenuItem
            ): Boolean {
                LogUtil.e("3333333333333333333333333333333333333333")
                val selStart: Int = tv_text2?.selectionStart ?: 0
                val selEnd: Int = tv_text2?.selectionEnd ?: 0
                val min = 0.coerceAtLeast(selStart.coerceAtMost(selEnd))
                val max = 0.coerceAtLeast(selStart.coerceAtLeast(selEnd))
                val content = tv_text2?.text?.subSequence(min, max)
                if (TextUtils.isEmpty(content)) {
                    return true
                }
                //根据item的ID处理点击事件
                when (menuItem.itemId) {
                    R.id.Informal22 -> {
                        Toast.makeText(
                            this@TextLongDownActivity2,
                            "点击的是22",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        actionMode.finish() //收起操作菜单
                    }

                    R.id.Informal33 -> {
                        Toast.makeText(
                            this@TextLongDownActivity2,
                            "点击的是33",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        actionMode.finish()
                    }
                }
                return false //返回true则系统的"复制"、"搜索"之类的item将无效，只有自定义item有响应
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                LogUtil.e("444444444444444444444444444444444444444444")
            }

            override fun onGetContentRect(mode: ActionMode?, view: View?, outRect: Rect?) {
                super.onGetContentRect(mode, view, outRect)
                LogUtil.e("555555555555555555555555555555" + outRect.toString())
            }
        }
    }
}