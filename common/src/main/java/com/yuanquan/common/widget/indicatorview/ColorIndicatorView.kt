package com.yuanquan.common.widget.indicatorview

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet

class ColorIndicatorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : BaseIndicatorView(context, attrs) {

    override fun onDrawIndicator(canvas: Canvas) {
        var itemLeft = 0

        for (i in 0 until mItemCount) {
            if (i == mActive) {
                item.setColor(evaluateColor(mProgress, mActiveColor, mNormalColor))
            } else if (i == mActive + mToward) {
                item.setColor(evaluateColor(mProgress, mNormalColor, mActiveColor))
            } else {
                item.setColor(mNormalColor)
            }
            item.setBounds(itemLeft, 0, itemLeft + mNormalWidth, mNormalHeight)
            item.draw(canvas)

            itemLeft += mGap + mNormalWidth
        }
    }
}