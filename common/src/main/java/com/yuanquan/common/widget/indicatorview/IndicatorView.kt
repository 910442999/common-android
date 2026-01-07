package com.yuanquan.common.widget.indicatorview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet

class IndicatorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : BaseIndicatorView(context, attrs) {

    companion object {
        const val MODE_SLIDE: Int = 0
        const val MODE_WORM: Int = 1
    }

    private var mMode: Int = MODE_WORM
    private var mRoundLine: Boolean = false

    fun setSlideMode(): IndicatorView {
        mMode = MODE_SLIDE
        return this
    }
    fun setWormMode(): IndicatorView {
        mMode = MODE_WORM
        return this
    }
    fun setRoundLine(value: Boolean = true): IndicatorView {
        mRoundLine = value
        return this
    }



    override fun onDrawIndicator(canvas: Canvas) {


        if (mRoundLine) {
            item.setColor(mNormalColor)
            item.setBounds(0, 0, measuredWidth, measuredHeight)
            item.draw(canvas)
        } else {
            item.setStroke(mStroke, mNormalColor)
            item.setColor(if (mStroke > 0) Color.TRANSPARENT else mNormalColor)

            val target = mActive + mToward
            val start = (if (mToward > 0) mActive else target) + 1
            val end = if (mToward > 0) target else mActive

            val itemTop = (mActiveHeight - mNormalHeight) / 2
            var itemLeft = 0
            val itemOffset = (if (mToward > 0) -1 else 1) * ((mActiveWidth - mNormalWidth) * mProgress).toInt()

            for (i in 0 until mItemCount) {
                val itemX = itemLeft + if (i in start..end) itemOffset else 0
                item.setBounds(itemX + 1, itemTop + 1, itemX + mNormalWidth - 1, itemTop + mNormalHeight - 1)
                item.draw(canvas)

                itemLeft += mGap + if (mActive == i) mActiveWidth else mNormalWidth
            }
            item.setStroke(0, 0)
        }



        val startX = (mNormalWidth + mGap) * mActive
        val deltaX = (mNormalWidth + mGap) * mToward

        when(mMode) {
            MODE_SLIDE -> {
                val x = startX + (deltaX * mProgress).toInt()
                item.setBounds(x, 0, x + mActiveWidth, mActiveHeight)
            }
            MODE_WORM -> {
                val x1 = startX + (deltaX * worm(mProgress, mToward < 0)).toInt()
                val x2 = startX + (deltaX * worm(mProgress, mToward > 0)).toInt()
                item.setBounds(x1, 0, x2 + mActiveWidth, mActiveHeight)
            }
        }

        item.setColor(mActiveColor)
        item.draw(canvas)
    }

    private fun worm(fraction: Float, isFirstMove: Boolean) = when {
        isFirstMove -> if (fraction < 0.5f) fraction * 2f else 1f
        else -> if (fraction < 0.5f) 0f else (fraction - 0.5f) * 2
    }
}