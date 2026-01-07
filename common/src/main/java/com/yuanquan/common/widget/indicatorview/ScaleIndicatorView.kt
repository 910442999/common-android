package com.yuanquan.common.widget.indicatorview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet

class ScaleIndicatorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : BaseIndicatorView(context, attrs) {


    override fun onDrawIndicator(canvas: Canvas) {

        item.setStroke(mStroke, mActiveColor)

        val fillColor = if (mStroke > 0) Color.TRANSPARENT else mNormalColor
        val deltaWidth = ((mActiveWidth - mNormalWidth) * mProgress).toInt()
        val deltaHeight = ((mActiveHeight - mNormalHeight) * mProgress).toInt()

        val target = mActive + mToward
        val start = (if (mToward > 0) mActive else target) + 1
        val end = if (mToward > 0) target else mActive

        val itemTop = (mActiveHeight - mNormalHeight) / 2
        var itemLeft = 0
        val itemOffset = (if (mToward > 0) -1 else 1) * ((mActiveWidth - mNormalWidth) * mProgress).toInt()

        for (i in 0 until mItemCount) {
            if (i == mActive) {
                val newWidth = mActiveWidth - deltaWidth
                val newHeight = mActiveHeight - deltaHeight
                val newLeft = itemLeft + if (mToward < 0) deltaWidth else 0
                val newTop = (mActiveHeight - newHeight) / 2

                item.setBounds(newLeft, newTop, newLeft + newWidth, newTop + newHeight)
                item.setColor(evaluateColor(mProgress, mActiveColor, fillColor))
            } else if (i == target) {
                val newWidth = mNormalWidth + deltaWidth
                val newHeight = mNormalHeight + deltaHeight
                val newLeft = itemLeft - if (mToward < 0) 0 else deltaWidth
                val newTop = (mActiveHeight - newHeight) / 2

                item.setBounds(newLeft, newTop, newLeft + newWidth, newTop + newHeight)
                item.setColor(evaluateColor(mProgress, fillColor, mActiveColor))
            } else {
                val newLeft = itemLeft + if (i in start .. end) itemOffset else 0
                item.setBounds(newLeft, itemTop, newLeft + mNormalWidth, itemTop + mNormalHeight)
                item.setColor(fillColor)
            }

            item.draw(canvas)

            itemLeft += mGap + if (mActive == i) mActiveWidth else mNormalWidth
        }
    }
}