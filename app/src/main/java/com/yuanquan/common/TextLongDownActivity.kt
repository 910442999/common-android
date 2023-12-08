package com.yuanquan.common

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import com.yuanquan.common.widget.SelectableTextHelper

class TextLongDownActivity : Activity() {
    private var mSelectableTextHelper: SelectableTextHelper? = null//实例化
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_long_down)
        var tv_text2 = findViewById<TextView>(R.id.tv_text2)
//text为文案
        mSelectableTextHelper = SelectableTextHelper.Builder(tv_text2)
            .setSelectedColor(Color.parseColor("#afe1f4"))
            .setCursorHandleSizeInDp(20f)
            .setCursorHandleColor(Color.parseColor("#0d7aff"))
            .build()
    }
}