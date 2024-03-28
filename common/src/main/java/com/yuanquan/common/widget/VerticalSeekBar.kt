package com.yuanquan.common.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar

class VerticalSeekBar : AppCompatSeekBar {
    private var mThumb: Drawable? = null
    private var mOnSeekBarChangeListener: OnSeekBarChangeListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun setOnSeekBarChangeListener(l: OnSeekBarChangeListener) {
        mOnSeekBarChangeListener = l
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        //倒置宽与高
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    /**
     * 逆时针转90度，并向下移整个控件的高度
     *
     * @param c
     */
    override fun onDraw(c: Canvas) {
        c.rotate(-90f)
        c.translate(-height.toFloat(), 0f)
        super.onDraw(c)
    }

    private fun onProgressRefresh(scale: Float, fromUser: Boolean) {
        var scale = scale
        if (scale < 0) {
            scale = 0f
        }
        if (scale > max) {
            scale = max.toFloat()
        }
        val thumb = mThumb
        if (thumb != null) {
            setThumbPos(height, thumb, scale)
            invalidate()
        }
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener!!.onProgressChanged(this, progress, fromUser)
        }
    }

    /**
     * 直接套用AbsSeekBar;唯一不同的是这里的
     * thumb的边界计算依旧是按原视图（左右）方向计算的，而不是以变换后的图形计算
     *
     * @param height
     * @param thumb
     * @param scale  [)][android.widget.AbsSeekBar]
     */
    private fun setThumbPos(height: Int, thumb: Drawable, scale: Float) {
        var available = height - paddingBottom - paddingTop
        val thumbWidth = thumb.intrinsicWidth
        val thumbHeight = thumb.intrinsicHeight
        available -= thumbHeight
        val thumbPos = (scale * available / 100 + 0.5f).toInt()
        val topBound: Int
        val bottomBound: Int
        val oldBounds = thumb.bounds
        topBound = oldBounds.top
        bottomBound = oldBounds.bottom
        thumb.setBounds(thumbPos, topBound, thumbPos + thumbHeight, bottomBound)
    }

    override fun setThumb(thumb: Drawable) {
        mThumb = thumb
        super.setThumb(thumb)
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

    private fun attemptClaimDrag() {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
    }

    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        onProgressRefresh(progress.toFloat(), false)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                onStartTrackingTouch()
            }

            MotionEvent.ACTION_MOVE -> {
                val progress = max - (max * event.y / height).toInt()
                setProgress(progress)
                attemptClaimDrag()
            }

            MotionEvent.ACTION_UP -> {
                onStopTrackingTouch()
                onProgressRefresh(progress.toFloat(), true)
                isPressed = false
            }

            MotionEvent.ACTION_CANCEL -> {
                onStopTrackingTouch()
                isPressed = false
            }
        }
        return true
    }
}