package com.yuanquan.common

import android.app.Activity
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast


class TextLongDownActivity2 : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_long_down2)
        var tv_text2 = findViewById<TextView>(R.id.tv_text2)
        tv_text2.setTextIsSelectable(true)
        tv_text2.customSelectionActionModeCallback = object : ActionMode.Callback2() {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return true //返回false则不会显示弹窗
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
                return false
            }

            override fun onActionItemClicked(
                actionMode: ActionMode,
                menuItem: MenuItem
            ): Boolean {
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

            override fun onDestroyActionMode(mode: ActionMode?) {}
        }
    }
}