package com.yuanquan.common.widget

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.yuanquan.common.R

class CustomMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById(R.id.tvContent)
//    private val tvTime: TextView = findViewById(R.id.tvTime) // 可选，用于显示时间

    override fun refreshContent(e: Entry, highlight: Highlight) {
        // 设置显示的内容，例如Y值
        tvContent.text = "Value: ${e.y}"
        // 如果需要，可以根据数据集索引设置不同的颜色或标签
        when (highlight.dataSetIndex) {
            0 -> tvContent.setTextColor(Color.parseColor("#4CAF50")) // 绿色数据集
            1 -> tvContent.setTextColor(Color.parseColor("#2196F3")) // 蓝色数据集
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        // 此方法用于调整MarkerView的显示位置，使其居中于点击点上方
        return MPPointF((-width / 2).toFloat(), (-height).toFloat())
    }
}