package com.yuanquan.common.widget.selecttext

import android.text.TextPaint
import android.text.style.MetricAffectingSpan


class CustomTextSpan(
    private val textColor: Int,
    private val textSize: Int,
) :
    MetricAffectingSpan() {
    override fun updateDrawState(tp: TextPaint) {
        tp.color = textColor
        tp.textSize = textSize.toFloat()
    }

    override fun updateMeasureState(tp: TextPaint) {
        tp.color = textColor
        tp.textSize = textSize.toFloat()
    }
}