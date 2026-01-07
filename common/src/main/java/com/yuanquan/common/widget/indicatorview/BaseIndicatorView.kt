package com.yuanquan.common.widget.indicatorview

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.max


abstract class BaseIndicatorView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    companion object {

        private val colorEvaluator = ArgbEvaluator()
        fun evaluateColor(fraction: Float, startColor: Int, endColor: Int): Int {
            return colorEvaluator.evaluate(fraction, startColor, endColor) as Int
        }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    protected val item = GradientDrawable()

    protected var mNormalWidth: Int = 6.dp
    protected var mActiveWidth: Int = 6.dp

    protected var mNormalHeight: Int = 6.dp
    protected var mActiveHeight: Int = 6.dp

    protected var mNormalColor: Int = Color.GRAY
    protected var mActiveColor: Int = Color.GREEN

    protected var mGap: Int = 5.dp
    protected var mStroke: Int = 0

    protected var mActive: Int = 0
    protected var mToward: Int = 0
    protected var mProgress: Float = 0f

    protected var mItemCount: Int = 0


    fun setOvalShape(): BaseIndicatorView {
        item.shape = GradientDrawable.OVAL
        return this
    }

    fun setRectShape(): BaseIndicatorView {
        item.shape = GradientDrawable.RECTANGLE
        return this
    }

    fun setCornerRadius(value: Int): BaseIndicatorView {
        item.cornerRadius = value.toFloat()
        return this
    }

    fun setStroke(value: Int): BaseIndicatorView {
        mStroke = value
        return this
    }

    fun setGap(value: Int): BaseIndicatorView {
        mGap = value
        return this
    }

    fun setColor(color: Int, activeColor: Int = color): BaseIndicatorView {
        mNormalColor = color
        mActiveColor = activeColor
        return this
    }

    fun setSize(
        size: Int, activeSize: Int = size,
        width: Int = size, activeWidth: Int = activeSize,
        height: Int = size, activeHeight: Int = activeSize,
    ): BaseIndicatorView {
        mNormalWidth = width
        mNormalHeight = height
        mActiveWidth = max(width, activeWidth)
        mActiveHeight = max(height, activeHeight)
        return this
    }

    fun setItemCount(value: Int) {
        mItemCount = value
        requestLayout()
    }

    fun update(position: Int, offset: Float) {

        if (offset == 0f) {
            mActive = position
            mToward = 0
        } else {
            val target = position + (if (position >= mActive) 1 else 0)
            if (mToward != 0 && mItemCount != 0) {
                mActive = (target - mToward + mItemCount) % mItemCount
            }
            mToward = target - mActive
        }
        mProgress = if (mToward < 0) (1 - offset) else offset
//        Log.e("OoO", "update($position, $offset) => active = $mActive, toward = $mToward, progress = $mProgress")
        requestLayout()
    }

    fun attachTo(view: ViewPager2) {
        val adapter = view.adapter ?: throw IllegalStateException("ViewPager2.adapter is null")
        setItemCount(adapter.itemCount)
        adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                setItemCount(adapter.itemCount)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = onChanged()
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = onChanged()
        })

        view.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                update(position, positionOffset)
            }

            override fun onPageSelected(position: Int) {
                if (view.scrollState == ViewPager2.SCROLL_STATE_IDLE) {
                    update(position, 0f)
                }
            }
        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mItemCount <= 1) return
        setMeasuredDimension(((mItemCount - 1) * (mNormalWidth + mGap) + mActiveWidth), mActiveHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mItemCount <= 1) return
        onDrawIndicator(canvas)
    }

    abstract fun onDrawIndicator(canvas: Canvas)

}