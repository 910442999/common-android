package com.yuanquan.common.widget.selecttext

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan

//class ZeroHeightSpan : DynamicDrawableSpan() {
//    override fun getDrawable(): Drawable {
//        return ColorDrawable(Color.TRANSPARENT) // 使用透明的 Drawable
//    }
//
//    override fun getSize(
//        paint: Paint,
//        text: CharSequence,
//        start: Int,
//        end: Int,
//        fm: Paint.FontMetricsInt?
//    ): Int {
//        return 0 // 返回零作为 Span 的宽度
//    }
//}

class ZeroHeightSpan : ReplacementSpan() {
    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        // 返回零作为文本的高度
        fm?.ascent = 0
        fm?.descent = 0
        fm?.top = 0
        fm?.bottom = 0
        return 0
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        // 不进行任何绘制操作
    }
}

// 使用示例
//val spannableString = SpannableString("This is a spannable text")
//val zeroHeightSpan = ZeroHeightSpan()
//spannableString.setSpan(zeroHeightSpan, 0, spannableString.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)