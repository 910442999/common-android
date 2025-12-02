package com.yuanquan.common.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout


// PageIndicatorView.java
class PageIndicatorView : LinearLayout {
    private var pageCount = 0
    private var currentPage = 0
    private var selectedColor = Color.RED
    private var unselectedColor = Color.GRAY
    private var indicatorSize = 20 // dp
    private var indicatorSpacing = 10 // dp

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        setOrientation(HORIZONTAL)
        setGravity(Gravity.CENTER)
    }

    fun setPageCount(count: Int) {
        this.pageCount = count
        updateIndicators()
    }

    fun setCurrentPage(page: Int) {
        if (page < 0 || page >= pageCount) return
        this.currentPage = page
        updateIndicators()
    }

    fun setColors(selectedColor: Int, unselectedColor: Int) {
        this.selectedColor = selectedColor
        this.unselectedColor = unselectedColor
        updateIndicators()
    }

    fun setIndicatorSize(size: Int) {
        this.indicatorSize = size
        updateIndicators()
    }

    fun setIndicatorSpacing(size: Int) {
        this.indicatorSpacing = size
        updateIndicators()
    }

    private fun updateIndicators() {
        removeAllViews()

        if (pageCount <= 1) {
            setVisibility(GONE)
            return
        }

        setVisibility(VISIBLE)
        val sizePx = dpToPx(indicatorSize)
        val spacingPx = dpToPx(indicatorSpacing)

        for (i in 0..<pageCount) {
            val indicator = View(getContext())
            val params = LayoutParams(sizePx, sizePx)
            params.setMargins(spacingPx / 2, 0, spacingPx / 2, 0)
            indicator.setLayoutParams(params)
            indicator.setBackground(createIndicatorDrawable(i == currentPage))
            addView(indicator)
        }
    }

    private fun createIndicatorDrawable(selected: Boolean): Drawable {
        val drawable = GradientDrawable()
        drawable.setShape(GradientDrawable.OVAL)
        drawable.setColor(if (selected) selectedColor else unselectedColor)
        drawable.setSize(dpToPx(indicatorSize), dpToPx(indicatorSize))
        return drawable
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * getResources().getDisplayMetrics().density).toInt()
    }
}