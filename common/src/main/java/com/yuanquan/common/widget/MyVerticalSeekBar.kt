package com.yuanquan.common.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar
import com.yuanquan.common.utils.LogUtil


class MyVerticalSeekBar : AppCompatSeekBar {
    private var mThumb: Drawable? = null

    interface OnSeekBarChangeListener {
        fun onProgressChanged(VerticalSeekBar: MyVerticalSeekBar?, progress: Int, fromUser: Boolean)
        fun onStartTrackingTouch(VerticalSeekBar: MyVerticalSeekBar?)
        fun onStopTrackingTouch(VerticalSeekBar: MyVerticalSeekBar?)
    }

    fun onStartTrackingTouch() {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener!!.onStartTrackingTouch(this)
        }
    }

    fun onStopTrackingTouch() {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener!!.onStopTrackingTouch(this)
        }
    }

    fun onProgressRefresh(scale: Float, fromUser: Boolean) {
        LogUtil.i("6", "onProgressRefresh==>scale$scale")
        val thumb = mThumb
        if (thumb != null) {
            setThumbPos(height, thumb, scale, Int.MIN_VALUE)
            invalidate()
        }
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener!!.onProgressChanged(this, progress, fromUser)
        }
    }
    override fun setThumb(thumb: Drawable) {
        mThumb = thumb
        super.setThumb(thumb)
    }
    fun setOnSeekBarChangeListener(l: OnSeekBarChangeListener?) {
        mOnSeekBarChangeListener = l
    }

    private fun setThumbPos(w: Int, thumb: Drawable, scale: Float, gap: Int) {
        Log.i("6", "setThumbPos==>w$w")
        var available = w + paddingLeft - paddingRight
        val thumbWidth = thumb.intrinsicWidth
        val thumbHeight = thumb.intrinsicHeight
        available -= thumbWidth
        // The extra space for the thumb to move on the track
        available += thumbOffset * 2
        val thumbPos = (scale * available).toInt()
        val topBound: Int
        val bottomBound: Int
        if (gap == Int.MIN_VALUE) {
            val oldBounds = thumb.bounds
            topBound = oldBounds.top
            bottomBound = oldBounds.bottom
        } else {
            topBound = gap
            bottomBound = gap + thumbHeight
        }
        thumb.setBounds(thumbPos, topBound, thumbPos + thumbWidth, bottomBound)
    }

    private var mOnSeekBarChangeListener: OnSeekBarChangeListener? = null

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(c: Canvas) {
        c.rotate(-90f)
        c.translate(-height.toFloat(), 0f)
        super.onDraw(c)
    }

    @Synchronized
    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        onSizeChanged(width, height, 0, 0)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.e("TAG", "onKeyDown: 22222222222222222222222")
                onStartTrackingTouch()
                progress = max - (max * event.y / height).toInt()
                onSizeChanged(width, height, 0, 0)
            }

            MotionEvent.ACTION_MOVE -> {
                progress = max - (max * event.y / height).toInt()
                onSizeChanged(width, height, 0, 0)
                onProgressRefresh(progress.toFloat(), false)
            }

            MotionEvent.ACTION_UP -> {
                onProgressRefresh(progress.toFloat(), true)
                onStopTrackingTouch()
                progress = max - (max * event.y / height).toInt()
                onSizeChanged(width, height, 0, 0)
            }

            MotionEvent.ACTION_CANCEL -> onStopTrackingTouch()
        }
        return true
    }
}